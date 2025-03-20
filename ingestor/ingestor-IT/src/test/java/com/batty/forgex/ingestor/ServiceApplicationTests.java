package com.batty.forgex.ingestor;

// import com.batty.service.api.DefaultApi;
// import com.batty.service.api.client.ApiException;
import com.batty.forgex.ingestor.api.DefaultApi;
import com.batty.forgex.ingestor.api.client.ApiClient;
import com.batty.forgex.ingestor.api.client.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;



class ServiceApplicationTests {


    // protected DefaultApi api;


	void contextLoads() throws ApiException {
        com.batty.forgex.ingestor.api.client.Configuration.setDefaultApiClient(new ApiClient().setBasePath("http://localhost:8080"));
		DefaultApi api  = new DefaultApi();
		api.addUser("BH");
	}

}
