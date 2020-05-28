package com.seongje.studyolle.domain.account;

import com.seongje.studyolle.domain.Tag;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "tag_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class TagItem {

    @Id @GeneratedValue
    @Column(name = "tag_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // 생성 메서드 //

    public static TagItem createTagItem(Account account, Tag tag) {
        return TagItem.builder().account(account).tag(tag).build();
    }
}
