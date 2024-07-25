package com.cengzayhn.product_service;

import com.cengzayhn.product_service.dto.response.ProductResponse;
import com.cengzayhn.product_service.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cengzayhn.product_service.dto.request.ProductRequest;
import com.cengzayhn.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;

	static {
		mongoDBContainer.start();
	}

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
		dymDynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("Product One")
				.description("Product One Description")
				.price(BigDecimal.valueOf(1300))
				.build();
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(status().isCreated());
		Assertions.assertEquals(1, productRepository.findAll().size());
	}


	@Test
	void shouldGetAllProducts() throws Exception{
		//Mock products to fill database
		Product mockProduct1 = new Product();
		mockProduct1.setName("Product One");
		mockProduct1.setDescription("Product One Description");
		mockProduct1.setPrice(BigDecimal.valueOf(1300));
		productRepository.save(mockProduct1);

		Product mockProduct2 = new Product();
		mockProduct2.setName("Product Two");
		mockProduct2.setDescription("Product Two Description");
		mockProduct2.setPrice(BigDecimal.valueOf(1400));
		productRepository.save(mockProduct2);

		String responseString = mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
		)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		List<ProductResponse> productResponses = objectMapper
				.readValue(responseString,objectMapper.getTypeFactory()
				.constructCollectionType(List.class,ProductResponse.class));

		Assertions.assertEquals(2, productResponses.size());
		Assertions.assertEquals(mockProduct1.getName(), productResponses.get(0).getName());
		Assertions.assertEquals(mockProduct1.getDescription(), productResponses.get(0).getDescription());
		Assertions.assertEquals(mockProduct1.getPrice(), productResponses.get(0).getPrice());
		Assertions.assertEquals(mockProduct2.getName(), productResponses.get(1).getName());
		Assertions.assertEquals(mockProduct2.getDescription(), productResponses.get(1).getDescription());
		Assertions.assertEquals(mockProduct2.getPrice(), productResponses.get(1).getPrice());
	}
}