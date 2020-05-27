package com.seongje.studyolle.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tag_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
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
        TagItem newTagItem = new TagItem();
        newTagItem.setAccount(account);
        newTagItem.setTag(tag);

        return newTagItem;
    }
}
