package com.demo.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/***
 * @Author: Mohamed Afallah
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "messages")
@ToString
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String content;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Builder.Default
    private Boolean isRead = false;
}
