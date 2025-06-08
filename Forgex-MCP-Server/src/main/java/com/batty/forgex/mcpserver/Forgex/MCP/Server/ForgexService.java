package com.batty.forgex.mcpserver.Forgex.MCP.Server;


import com.batty.forgex.ingestor.api.model.GraphInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;


@Service
public class ForgexService {

    protected Logger log = LoggerFactory.getLogger(ForgexService.class);

    private final RestClient restClient;

    public ObjectMapper mapper;

    public ForgexService() {
        mapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081/graph/process")
                .build();
    }


    @Tool(description = "Deploy a forgex service")
    public String generateForgexService(String graphInput) {
        String status = "Failed";
        try {
            String json= """
                    {
                     "nodes": [
                       {
                         "type": "ENTITY",
                         "name": "User",
                         "fields": [
                           {
                             "name": "id",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "email",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "firstName",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "lastName",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "createdAt",
                             "type": "date",
                             "required": true
                           },
                           {
                             "name": "isActive",
                             "type": "boolean",
                             "required": true
                           }
                         ]
                       },
                       {
                         "type": "ENTITY",
                         "name": "Category",
                         "fields": [
                           {
                             "name": "id",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "name",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "description",
                             "type": "string",
                             "required": false
                           },
                           {
                             "name": "color",
                             "type": "string",
                             "required": false
                           },
                           {
                             "name": "isDefault",
                             "type": "boolean",
                             "required": true
                           }
                         ]
                       },
                       {
                         "type": "ENTITY",
                         "name": "Expense",
                         "fields": [
                           {
                             "name": "id",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "amount",
                             "type": "number",
                             "required": true
                           },
                           {
                             "name": "description",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "transactionType",
                             "type": "string",
                             "required": true
                           },
                           {
                             "name": "transactionDate",
                             "type": "date",
                             "required": true
                           },
                           {
                             "name": "createdAt",
                             "type": "date",
                             "required": true
                           },
                           {
                             "name": "isRecurring",
                             "type": "boolean",
                             "required": false
                           },
                           {
                             "name": "notes",
                             "type": "string",
                             "required": false
                           }
                         ]
                       }
                     ],
                     "edges": [
                       {
                         "source": "Expense",
                         "target": "User",
                         "relationship": "belongsTo"
                       },
                       {
                         "source": "Expense",
                         "target": "Category",
                         "relationship": "linkedTo"
                       },
                       {
                         "source": "User",
                         "target": "Category",
                         "relationship": "contains"
                       }
                     ],
                     "businessRules": [
                       {
                         "name": "validTransactionType",
                         "description": "Transaction type must be either 'debit' or 'credit'",
                         "entity": "Expense",
                         "field": "transactionType"
                       },
                       {
                         "name": "positiveAmount",
                         "description": "Amount must be greater than zero",
                         "entity": "Expense",
                         "field": "amount"
                       }
                     ],
                     "events": [
                       {
                         "name": "expenseCreated",
                         "trigger": "onCreate",
                         "entity": "Expense"
                       },
                       {
                         "name": "monthlyReport",
                         "trigger": "scheduled",
                         "schedule": "monthly"
                       }
                     ]
                    }
                    """;

            GraphInput input = mapper.readValue(json, GraphInput.class); // <-- use input
            log.info("Sending to ingestor: {}", input);
            String response = this.restClient.post()
                    .uri("") // since full path is in baseUrl
                    .header("Content-Type", "application/json")
                    .body(input)
                    .retrieve()
                    .body(String.class);

            status = "Success: " + response;
        } catch (Exception e) {
            log.error("Error invoking ingestor", e);
            return status + " - " + e.getMessage();
        }
        return status;
    }

/*

    @Tool(description = "Deploy a forgex service")
    public String generateForgexService(String graphInput) {
        String status = "Failed";
        try {
            JsonNode root = mapper.readTree(graphInput);

            // Get nodes
            JsonNode nodes = root.path("nodes");

            List<Map<String, Object>> entityNodes = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = nodes.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String nodeName = entry.getKey();
                JsonNode nodeValue = entry.getValue();

                String type = nodeValue.path("type").asText();
                if ("microservice".equalsIgnoreCase(type)) {
                    // Convert endpoints to fields
                    List<Map<String, Object>> fieldsList = new ArrayList<>();
                    JsonNode endpoints = nodeValue.path("endpoints");
                    if (endpoints.isArray()) {
                        for (JsonNode ep : endpoints) {
                            String[] parts = ep.asText().split(" ");
                            if (parts.length == 2) {
                                Map<String, Object> field = new HashMap<>();
                                field.put("name", parts[1].replaceAll("[:/]", "_")); // make it safe
                                field.put("type", "string"); // assume type
                                field.put("required", false);
                                fieldsList.add(field);
                            }
                        }
                    }

                    Map<String, Object> entity = new HashMap<>();
                    entity.put("type", "ENTITY");
                    entity.put("name", toEntityName(nodeName));
                    entity.put("fields", fieldsList);
                    entityNodes.add(entity);
                }
            }

            // Build minimal GraphInput-like structure
            Map<String, Object> graphInputMap = new HashMap<>();
            graphInputMap.put("nodes", entityNodes);
            graphInputMap.put("edges", new ArrayList<>()); // optional
            graphInputMap.put("businessRules", new ArrayList<>()); // optional
            graphInputMap.put("events", new ArrayList<>()); // optional

            GraphInput input = mapper.convertValue(graphInputMap, GraphInput.class);
            log.info("Extracted GraphInput for ingestor: {}", input);

            String response = this.restClient.post()
                    .uri("")
                    .header("Content-Type", "application/json")
                    .body(input)
                    .retrieve()
                    .body(String.class);

            status = "Success: " + response;
        } catch (Exception e) {
            log.error("Error parsing or invoking ingestor", e);
            return status + " - " + e.getMessage();
        }
        return status;
    }
*/
    private String toEntityName(String serviceName) {
        String[] parts = serviceName.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1));
        }
        return sb.toString().replaceAll("Service", "");
    }


    public static void main(String args[]) {
        ForgexService fs = new ForgexService();
        String data = """
                {
                  `graphInput`: `{
                  \\"nodes\\": [
                    {
                      \\"type\\": \\"ENTITY\\",
                      \\"name\\": \\"Item\\",
                      \\"fields\\": [
                        {\\"name\\": \\"itemId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"name\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"description\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"price\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"sku\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"category\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"stockQuantity\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"isActive\\", \\"type\\": \\"boolean\\", \\"required\\": true},
                        {\\"name\\": \\"barcode\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"cost\\", \\"type\\": \\"number\\", \\"required\\": false}
                      ]
                    },
                    {
                      \\"type\\": \\"ENTITY\\",
                      \\"name\\": \\"Transaction\\",
                      \\"fields\\": [
                        {\\"name\\": \\"transactionId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"transactionNumber\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"customerId\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"cashierId\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"subtotal\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"taxAmount\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"discountAmount\\", \\"type\\": \\"number\\", \\"required\\": false},
                        {\\"name\\": \\"totalAmount\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"paymentMethod\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"status\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"transactionDate\\", \\"type\\": \\"date\\", \\"required\\": true}
                      ]
                    },
                    {
                      \\"type\\": \\"ENTITY\\",
                      \\"name\\": \\"TransactionItem\\",
                      \\"fields\\": [
                        {\\"name\\": \\"transactionItemId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"transactionId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"itemId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"quantity\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"unitPrice\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"lineTotal\\", \\"type\\": \\"number\\", \\"required\\": true},
                        {\\"name\\": \\"discount\\", \\"type\\": \\"number\\", \\"required\\": false}
                      ]
                    },
                    {
                      \\"type\\": \\"ENTITY\\",
                      \\"name\\": \\"Customer\\",
                      \\"fields\\": [
                        {\\"name\\": \\"customerId\\", \\"type\\": \\"string\\", \\"required\\": true},
                        {\\"name\\": \\"firstName\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"lastName\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"email\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"phone\\", \\"type\\": \\"string\\", \\"required\\": false},
                        {\\"name\\": \\"loyaltyPoints\\", \\"type\\": \\"number\\", \\"required\\": false}
                      ]
                    }
                  ],
                  \\"edges\\": [
                    {
                      \\"source\\": \\"Transaction\\",
                      \\"target\\": \\"Customer\\",
                      \\"relationship\\": \\"belongsTo\\"
                    },
                    {
                      \\"source\\": \\"TransactionItem\\",
                      \\"target\\": \\"Transaction\\",
                      \\"relationship\\": \\"belongsTo\\"
                    },
                    {
                      \\"source\\": \\"TransactionItem\\",
                      \\"target\\": \\"Item\\",
                      \\"relationship\\": \\"references\\"
                    }
                  ],
                  \\"businessRules\\": [
                    {
                      \\"entity\\": \\"TransactionItem\\",
                      \\"name\\": \\"validateItemStock\\",
                      \\"conditions\\": [
                        {\\"field\\": \\"item.stockQuantity\\", \\"operator\\": \\">=\\", \\"value\\": \\"quantity\\"}
                      ],
                      \\"actions\\": [
                        {\\"type\\": \\"error\\", \\"message\\": \\"Insufficient stock for this item\\"}
                      ]
                    },
                    {
                      \\"entity\\": \\"TransactionItem\\",
                      \\"name\\": \\"calculateLineTotal\\",
                      \\"conditions\\": [],
                      \\"actions\\": [
                        {\\"type\\": \\"calculate\\", \\"formula\\": \\"quantity * unitPrice - discount\\"}
                      ]
                    },
                    {
                      \\"entity\\": \\"Transaction\\",
                      \\"name\\": \\"calculateTotalAmount\\",
                      \\"conditions\\": [],
                      \\"actions\\": [
                        {\\"type\\": \\"calculate\\", \\"formula\\": \\"subtotal + taxAmount - discountAmount\\"}
                      ]
                    },
                    {
                      \\"entity\\": \\"Item\\",
                      \\"name\\": \\"updateStockOnSale\\",
                      \\"conditions\\": [
                        {\\"field\\": \\"transaction.status\\", \\"operator\\": \\"==\\", \\"value\\": \\"completed\\"}
                      ],
                      \\"actions\\": [
                        {\\"type\\": \\"update\\", \\"field\\": \\"stockQuantity\\", \\"operation\\": \\"subtract\\", \\"value\\": \\"transactionItem.quantity\\"}
                      ]
                    }
                  ],
                  \\"events\\": [
                    {
                      \\"name\\": \\"transactionCreated\\",
                      \\"entity\\": \\"Transaction\\",
                      \\"fields\\": [\\"transactionId\\", \\"customerId\\", \\"totalAmount\\", \\"status\\"]
                    },
                    {
                      \\"name\\": \\"transactionCompleted\\",
                      \\"entity\\": \\"Transaction\\",
                      \\"fields\\": [\\"transactionId\\", \\"totalAmount\\", \\"paymentMethod\\", \\"transactionDate\\"]
                    },
                    {
                      \\"name\\": \\"itemStockUpdated\\",
                      \\"entity\\": \\"Item\\",
                      \\"fields\\": [\\"itemId\\", \\"stockQuantity\\"]
                    },
                    {
                      \\"name\\": \\"transactionItemAdded\\",
                      \\"entity\\": \\"TransactionItem\\",
                      \\"fields\\": [\\"transactionItemId\\", \\"transactionId\\", \\"itemId\\", \\"quantity\\", \\"lineTotal\\"]
                    }
                  ]
                }`
                }
                """;
        System.out.println(fs.generateForgexService(data));
    }
}