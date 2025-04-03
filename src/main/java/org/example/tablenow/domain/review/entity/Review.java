package org.example.tablenow.domain.review.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageUrl;
    private int rating;
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;
}
