package com.example.cw.product;
import com.example.cw.services.CreateProductService;
import com.example.cw.services.DeleteProductService;
import com.example.cw.services.GetProductService;
import com.example.cw.services.UpdateProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    private final CreateProductService createProductService;
    private final GetProductService getProductService;
    private final UpdateProductService updateProductService;
    private final DeleteProductService deleteProductService;

    public ProductController(CreateProductService createProductService,
                             GetProductService getProductService,
                             UpdateProductService updateProductService,
                             DeleteProductService deleteProductService) {
        this.createProductService = createProductService;
        this.getProductService = getProductService;
        this.updateProductService = updateProductService;
        this.deleteProductService = deleteProductService;
    }



    @PostMapping
    public ResponseEntity<String> createProduct() {
        return createProductService.execute(null);
    }

    @GetMapping
    public ResponseEntity<String> getProduct()
    {
        return getProductService.execute(null);
    }


    @PutMapping
    public ResponseEntity<String> updateProduct()
    {
        return updateProductService.execute(null);
    }


    @DeleteMapping
    public ResponseEntity<String> deleteProduct()
    {
        return deleteProductService.execute(null);
    }
}
