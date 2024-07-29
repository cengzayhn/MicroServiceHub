package com.cengzayhn.order_service;

import com.cengzayhn.order_service.dto.OrderLineItemsDTO;
import com.cengzayhn.order_service.dto.request.OrderRequest;
import com.cengzayhn.order_service.model.Order;
import com.cengzayhn.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	private OrderRequest getOrderRequest(){
		OrderLineItemsDTO mockItem1 = new OrderLineItemsDTO();
		mockItem1.setSkuCode("ABC123");
		mockItem1.setPrice(BigDecimal.valueOf(10.00));
		mockItem1.setQuantity(2);

		OrderLineItemsDTO mockItem2 = new OrderLineItemsDTO();
		mockItem2.setSkuCode("XYZ789");
		mockItem2.setPrice(BigDecimal.valueOf(20.00));
		mockItem2.setQuantity(1);

		return new OrderRequest(List.of(mockItem1, mockItem2));
	}

	@Test
	void shouldPlaceOrder() throws Exception{
		OrderRequest orderRequest = getOrderRequest();
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderRequestString)
		).andExpect(status().isCreated());

		Assertions.assertEquals(1, orderRepository.findAll().size());
	}

	@Test
	void shouldGetAllOrders() throws Exception{
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());

		OrderLineItemsDTO mockItem1 = new OrderLineItemsDTO();
		mockItem1.setSkuCode("ABC123");
		mockItem1.setPrice(BigDecimal.valueOf(10.00));
		mockItem1.setQuantity(2);

		OrderLineItemsDTO mockItem2 = new OrderLineItemsDTO();
		mockItem2.setSkuCode("XYZ789");
		mockItem2.setPrice(BigDecimal.valueOf(20.00));
		mockItem2.setQuantity(1);

		OrderRequest orderRequest = new OrderRequest(List.of(mockItem1, mockItem2));
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderRequestString)
		).andExpect(status().isCreated());

		String responseString = mockMvc.perform(MockMvcRequestBuilders.get("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
		).andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		List<Order> orders = objectMapper.readValue(responseString, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
		Assertions.assertEquals(1, orders.size());
		Assertions.assertEquals(order.getOrderNumber(), orders.get(0).getOrderNumber());
		Assertions.assertEquals(2, orders.get(0).getOrderLineItemsList().size());
	}
}
