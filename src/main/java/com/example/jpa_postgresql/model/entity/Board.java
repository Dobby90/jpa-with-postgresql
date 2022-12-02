package com.example.jpa_postgresql.model.entity;

import com.example.jpa_postgresql.framework.annotation.ColumnPosition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * <pre>
 * com.example.jpa_postgresql.entity
 * └ Sample
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-12-01
 **/
@Entity
@Table(schema = "public", name = "board_mst")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        schema = "public",
        name = "seq_board_mst",
        sequenceName = "seq_board_mst",
        allocationSize = 1
)
public class Board {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_board_mst"
    )
    @Column(name = "board_idx")
    @Comment("게시판_번호")
    @ColumnPosition(1)
    private long boardIdx;
    @CreatedDate
    @Column(name = "reg_dt", updatable = false)
    @Comment("등록_일시")
    @ColumnPosition(2)
    private LocalDateTime regDt;
    @Column(name = "reg_user", length = 20, updatable = false)
    @Comment("등록_자")
    @ColumnPosition(3)
    private String regUser;
    @LastModifiedDate
    @Column(name = "mod_dt")
    @Comment("수정_일시")
    @ColumnPosition(4)
    private LocalDateTime modDt;
    @Column(name = "mod_user", length = 20)
    @Comment("수정_자")
    @ColumnPosition(5)
    private String modUser;
}
