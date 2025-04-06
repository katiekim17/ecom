package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse(1L, "맥북", 50, 100000);
    }

    @Test
    @DisplayName("productId에 해당하는 상품이 있는 경우 해당 상품을 반환한다.")
    void get_api_v1_products_have_id_200() throws Exception {
        //given
        Long productId = productResponse.productId();
        Product product = Product.builder().id(productId).name("사과").price(5000).stock(50).build();
        when(productService.find(productId)).thenReturn(product);

        //when //then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{productId}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.name").value(product.getName()))
                .andExpect(jsonPath("$.stock").value(product.getStock()))
                .andExpect(jsonPath("$.price").value(product.getPrice()))
            ;
    }

    @DisplayName("모든 상품 목록을 반환한다.")
    @Test
    void get_api_v1_products_200() throws Exception{
        // given
        ProductResponse response = productResponse;

        //when //then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].productId").value(response.productId()))
                .andExpect(jsonPath("$.[0].name").value(response.name()))
                .andExpect(jsonPath("$.[0].stock").value(response.stock()))
                .andExpect(jsonPath("$.[0].price").value(response.price()))
        ;
    }



}