package org.example.tablenow.global.dummy;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.repository.CategoryRepository;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        ;

        //  유저 1
        User user1 = User.builder()
            .email("user1@email.com")
            .password(passwordEncoder.encode("Test1234!"))
            .name("유저1")
            .nickname("유저1")
            .phoneNumber("01011112222")
            .userRole(UserRole.ROLE_USER)
            .build();
        userRepository.save(user1);

        // 유저 2
        User user2 = User.builder()
            .email("user2@email.com")
            .password(passwordEncoder.encode("Test1234!"))
            .name("유저2")
            .nickname("유저2")
            .phoneNumber("01012345678")
            .userRole(UserRole.ROLE_USER)
            .build();
        userRepository.save(user2);

        //  사장
        User owner = User.builder()
            .email("owner1@email.com")
            .password(passwordEncoder.encode("Test1234!"))
            .name("사장1")
            .nickname("사장1")
            .phoneNumber("01033334444")
            .userRole(UserRole.ROLE_OWNER)
            .build();
        userRepository.save(owner);

        //  관리자
        User admin = User.builder()
            .email("admin1@email.com")
            .password(passwordEncoder.encode("Test1234!"))
            .name("관리자")
            .nickname("관리자")
            .phoneNumber("01055556666")
            .userRole(UserRole.ROLE_ADMIN)
            .build();
        userRepository.save(admin);

        // 카테고리
        Category category = Category.builder()
            .name("한식")
            .build();
        categoryRepository.save(category);

        //  가게
        Store store = Store.builder()
            .name("테스트용 가게")
            .description("스웨거 테스트용 더미 가게입니다.")
            .address("서울시 강남구")
            .imageUrl("https://test-image.com/store.jpg")
            .capacity(10)
            .startTime(LocalTime.of(11, 0))
            .endTime(LocalTime.of(22, 0))
            .deposit(1000)
            .user(owner)
            .category(category)
            .build();
        storeRepository.save(store);


    }

}

