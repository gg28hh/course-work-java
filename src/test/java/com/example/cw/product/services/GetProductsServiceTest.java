package com.example.cw.product.services;

import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.ProductSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private GetProductsService getProductsService;

    @Test
    void shouldReturnProductsSuccessfully() {

        Product product = new Product();
        ProductDTO productDTO = mock(ProductDTO.class);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop",
                1,
                100.0,
                2000.0,
                true,
                1,
                5,
                "price",
                "desc"
        );

        Page<Product> productPage = new PageImpl<>(
                List.of(product)
        );

        when(productRepository.searchProducts(
                eq("Laptop"),
                eq(1),
                eq(100.0),
                eq(2000.0),
                eq(true),
                any(Pageable.class)
        )).thenReturn(productPage);

        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ResponseEntity<?> response = getProductsService.execute(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productRepository, times(1)).searchProducts(
                eq("Laptop"),
                eq(1),
                eq(100.0),
                eq(2000.0),
                eq(true),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("price").getDirection()
        );

        verify(productMapper, times(1)).toDTO(product);
    }

    @Test
    void shouldThrowExceptionWhenPageIsNegative() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null,
                -1, 10, "id", "asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowExceptionWhenSizeIsInvalid() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null,
                0, 0, "id", "asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowExceptionWhenSizeIsGreaterThan100() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null,
                0, 101, "id", "asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowExceptionWhenMinPriceIsGreaterThanMaxPrice() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, 1000.0, 500.0, null,
                0, 10, "id", "asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowExceptionWhenSortFieldIsInvalid() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null,
                0, 10, "invalid", "asc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowExceptionWhenDirectionIsInvalid() {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null,
                0, 10, "id", "invalid"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getProductsService.execute(criteria)
        );

        verifyNoInteractions(productRepository, productMapper);
    }
}
