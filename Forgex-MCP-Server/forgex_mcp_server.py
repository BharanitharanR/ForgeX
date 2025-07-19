#!/usr/bin/env python3

import asyncio
import json
import logging
from typing import Any, Dict, List, Optional
from pathlib import Path
import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import uvicorn
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import (
    Tool,
    TextContent,
    CallToolResult,
    CallToolRequest,
)
from fastapi.middleware.cors import CORSMiddleware
# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Pydantic models matching your OpenAPI spec
class Field(BaseModel):
    name: str
    type: str
    required: bool

class Condition(BaseModel):
    field: str
    operator: str
    value: Any

class Action(BaseModel):
    type: str
    message: str

class Node(BaseModel):
    type: str  # Should be one of: ENTITY, RULE, EVENT
    name: str
    fields: List[Field] = []
    conditions: List[Condition] = []
    actions: List[Action] = []

class Edge(BaseModel):
    source: str
    target: str
    relationship: str

class GraphInput(BaseModel):
    nodes: List[Node]
    edges: List[Edge]

class GraphProcessResponse(BaseModel):
    message: str

class ErrorResponse(BaseModel):
    error: str

class IngestorMCPServer:
    """MCP Server for Ingestor Service Graph Processing"""

    def __init__(self, service_url: str = "http://localhost:8081", port: int = 8000):
        self.service_url = service_url
        self.port = port
        self.server = Server("ingestor-mcp-server")

        self.fastapi_app = FastAPI(
            title="Ingestor MCP Server",
            description="MCP Server for processing graph-based input to generate microservices and REST endpoints",
            version="1.0.0"
        )
        # Then add CORS middleware
        self.fastapi_app.add_middleware(
            CORSMiddleware,
            allow_origins=["*"],  # Be more specific in production
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )
        self._setup_fastapi_routes()
        self._setup_mcp_tools()

    def _setup_fastapi_routes(self):
        """Setup FastAPI routes for OpenAPI endpoints"""

        @self.fastapi_app.get("/")
        async def root():
            return {"message": "Ingestor MCP Server is running"}

        @self.fastapi_app.get("/health")
        async def health_check():
            return {"status": "healthy", "service": "ingestor-mcp-server"}

        @self.fastapi_app.get("/openapi.json")
        async def get_openapi_spec():
            """Return the OpenAPI specification as JSON"""
            return JSONResponse(self.fastapi_app.openapi())

        @self.fastapi_app.post("/graph/process", response_model=GraphProcessResponse)
        async def create_an_application(graph_input: GraphInput):
            """Create an application"""
            try:
                result = await self._forward_to_service(graph_input.dict())
                return GraphProcessResponse(message=result.get("message", "Created an Application successfully"))
            except Exception as e:
                logger.error(f"Error processing graph: {e}")
                raise HTTPException(status_code=400, detail=str(e))

    def _setup_mcp_tools(self):
        """Setup MCP tools for graph processing"""

        @self.server.call_tool()
        async def process_graph(arguments: Dict[str, Any]) -> CallToolResult:
            """
            Process a graph input to generate microservices and REST endpoints.

            Args:
                arguments: Dictionary containing 'nodes' and 'edges' arrays

            Expected format:
            {
                "nodes": [
                    {
                        "type": "ENTITY",
                        "name": "Employee",
                        "fields": [
                            {"name": "employeeId", "type": "string", "required": true}
                        ],
                        "conditions": [],
                        "actions": []
                    }
                ],
                "edges": [
                    {
                        "source": "Employee",
                        "target": "Department",
                        "relationship": "belongsTo"
                    }
                ]
            }
            """
            try:
                # Validate input structure
                if 'nodes' not in arguments or 'edges' not in arguments:
                    raise ValueError("Input must contain 'nodes' and 'edges' arrays")

                # Parse and validate using Pydantic
                graph_input = GraphInput(**arguments)

                # Forward to your service
                result = await self._forward_to_service(graph_input.dict())

                return CallToolResult(
                    content=[TextContent(
                        type="text",
                        text=json.dumps({
                            "status": "success",
                            "message": result.get("message", "Graph processed successfully"),
                            "service_url": f"{self.service_url}/graph/process"
                        }, indent=2)
                    )]
                )

            except ValueError as e:
                logger.error(f"Validation error: {e}")
                return CallToolResult(
                    content=[TextContent(
                        type="text",
                        text=json.dumps({
                            "status": "error",
                            "error": f"Validation error: {str(e)}",
                            "expected_format": {
                                "nodes": [
                                    {
                                        "type": "ENTITY|RULE|EVENT",
                                        "name": "string",
                                        "fields": [{"name": "string", "type": "string", "required": "boolean"}],
                                        "conditions": [{"field": "string", "operator": "string", "value": "any"}],
                                        "actions": [{"type": "string", "message": "string"}]
                                    }
                                ],
                                "edges": [
                                    {
                                        "source": "string",
                                        "target": "string",
                                        "relationship": "string"
                                    }
                                ]
                            }
                        }, indent=2)
                    )]
                )
            except Exception as e:
                logger.error(f"Error processing graph: {e}")
                return CallToolResult(
                    content=[TextContent(
                        type="text",
                        text=json.dumps({
                            "status": "error",
                            "error": str(e)
                        }, indent=2)
                    )]
                )

    async def _forward_to_service(self, graph_data: Dict[str, Any]) -> Dict[str, Any]:
        """Forward the graph processing request to your service"""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    f"{self.service_url}/graph/process",
                    json=graph_data,
                    headers={"Content-Type": "application/json"},
                    timeout=30.0
                )

                if response.status_code == 200:
                    return response.json()
                else:
                    error_detail = response.text
                    raise HTTPException(
                        status_code=response.status_code,
                        detail=f"Service error: {error_detail}"
                    )

            except httpx.TimeoutException:
                raise HTTPException(
                    status_code=504,
                    detail="Service timeout"
                )
            except httpx.RequestError as e:
                raise HTTPException(
                    status_code=503,
                    detail=f"Service unavailable: {str(e)}"
                )

    async def run_mcp_server(self):
        """Run the MCP server via stdio"""
        logger.info("Starting MCP server...")
        async with stdio_server() as (read_stream, write_stream):
            await self.server.run(read_stream, write_stream, self.server.create_initialization_options())

    def run_fastapi_server(self):
        """Run the FastAPI server"""
        logger.info(f"Starting FastAPI server on port {self.port}")
        uvicorn.run(self.fastapi_app, host="0.0.0.0", port=self.port)

    async def run_both_servers(self):
        """Run both MCP and FastAPI servers concurrently"""
        logger.info("Starting both MCP and FastAPI servers...")

        # Run FastAPI server in background
        import threading
        fastapi_thread = threading.Thread(target=self.run_fastapi_server)
        fastapi_thread.daemon = True
        fastapi_thread.start()

        # Run MCP server in main thread
        await self.run_mcp_server()

# Usage examples and entry point
if __name__ == "__main__":
    import sys

    # Configuration
    SERVICE_URL = "http://ingestor-service-1:8081"  # Your ingestor service URL
    PORT = 8000  # Port for the MCP server's FastAPI endpoints

    server = IngestorMCPServer(service_url=SERVICE_URL, port=PORT)

    if len(sys.argv) > 1 and sys.argv[1] == "fastapi":
        # Run only FastAPI server for testing
        server.run_fastapi_server()
    elif len(sys.argv) > 1 and sys.argv[1] == "mcp":
        # Run only MCP server
        asyncio.run(server.run_mcp_server())
    else:
        # Run both servers (default for OpenWebUI integration)
        asyncio.run(server.run_both_servers())