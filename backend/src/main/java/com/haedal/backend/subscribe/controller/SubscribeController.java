package com.haedal.backend.subscribe.controller;

import com.haedal.backend.auth.model.User;
import com.haedal.backend.auth.repository.UserRepository;
import com.haedal.backend.auth.service.UserService;
import com.haedal.backend.product.dto.response.ProductResponse;
import com.haedal.backend.product.model.Product;
import com.haedal.backend.product.service.ProductService;
import com.haedal.backend.profile.service.ProfileService;
import com.haedal.backend.subscribe.dto.response.SubscribeResponse;
import com.haedal.backend.subscribe.model.Subscribe;
import com.haedal.backend.subscribe.service.SubscribeService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/subscribe")
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://13.209.167.190"})
public class SubscribeController {
    private SubscribeService subscribeService;
    private ProfileService profileService;
    private ProductService productService;

    public SubscribeController(SubscribeService subscribeService, ProfileService profileService, ProductService productService) {
        this.subscribeService = subscribeService;
        this.profileService = profileService;
        this.productService = productService;
    }


    //신청 상품 정보 표출
    @GetMapping("/{productId}")
    public ProductResponse showSubcribeProduct(Authentication authentication, @PathVariable(name = "productId") Long productId){
        String id = authentication.getName();
        User user = profileService.findById(id);

        Product foundProduct = productService.findByProductId(productId);
        System.out.println(productId + "정보 조회");

        ProductResponse productResponse = ProductResponse.builder().productId(foundProduct.getProductId())
                .productAsset(foundProduct.getMaxProductMoney())
                .tag(foundProduct.getTag())
                .productName(foundProduct.getProductName())
                .shortInfo(foundProduct.getShortInfo())
                .longInfo(foundProduct.getLongInfo())
                .period(foundProduct.getPeriod())
                .interestRate(foundProduct.getInterestRate())
                .requiredStartMoney(foundProduct.getRequiredStartMoney())
                .maxProductMoney(foundProduct.getMaxProductMoney())
                .isDeposit(foundProduct.isDeposit())
                .subscription(foundProduct.getSubscription())
                .accountNumber(user.getAccountNumber())
                .build();

        System.out.println(productResponse);
        return productResponse;
    }


    //상품 신청 페이지 '신청'버튼 클릭
    @PostMapping("/{productId}/*")
    public ResponseEntity<String> subscribeproduct(Authentication authentication , @PathVariable Long productId, @RequestBody Map<String, String> requestData){
        //인증에 맞춰 정보 수정
        String id = authentication.getName();
        User user = profileService.findById(id);

        System.out.println(requestData);
        Product foundProduct = productService.findByProductId(productId);

        System.out.println(productId + " 접근 성공했다.");

        long authNumber = Long.parseLong(requestData.get("authenticationNumber"));
        long startMoney = Long.parseLong(requestData.get("startMoney"));
        
        //user가 입력한 인증번호가 DB와 같고, user_start_money(입력값)이 userAsset(본인 소유 자산)이상, productAsset(최대 금액)이하일 때
        if(authNumber == user.getAuthNumber() && user.getAsset() >= startMoney && startMoney<=foundProduct.getMaxProductMoney()){
            Subscribe subscribe = new Subscribe(user, foundProduct , startMoney, startMoney, LocalDate.now());
            Subscribe saveSubscribe = subscribeService.save(subscribe);
            System.out.println(saveSubscribe);
            return ResponseEntity.ok("신청이 완료되었습니다.");
        } else{
            return ResponseEntity.badRequest().body("신청 정보가 올바르지 않습니다.");
        }
    }

}
