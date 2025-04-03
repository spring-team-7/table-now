package org.example.tablenow.domain.store.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalTime;

@Getter
@Entity
@Table(name = "store")
public class Store extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String address;
    private String imageUrl;
    private int capacity;
    private LocalTime startTime;
    private LocalTime endTime;
    private int deposit;
}
