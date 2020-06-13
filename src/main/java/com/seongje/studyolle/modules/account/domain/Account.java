package com.seongje.studyolle.modules.account.domain;

import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    @Column(name = "account_id")
    private Long id;

    // 가입 정보 //

    @Column(unique = true)
    private String nickname;

    private String password;

    @Column(unique = true)
    private String email;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    // 회원 정보 //

    private String aboutMe;

    private String url;

    private String occupation;

    private String location;

    @Lob
    private String profileImg;

    private LocalDateTime nicknameLastChangedAt;

    // 알림 설정 //

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagItem> tags = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ZoneItem> zones = new HashSet<>();

    // 연관 관계 편의 메서드 //

    public void addTagItem(TagItem tagItem) {
        this.tags.add(tagItem);
        tagItem.setAccount(this);
    }

    public void removeTagItem(Tag tag) {
        this.tags.removeIf(tagItem -> tagItem.getTag().equals(tag));
    }

    public void addZoneItem(ZoneItem zoneItem) {
        this.zones.add(zoneItem);
        zoneItem.setAccount(this);
    }

    public void removeZoneItem(Zone zone) {
        this.zones.removeIf(zoneItem -> zoneItem.getZone().equals(zone));
    }

    // 비지니스 메서드 //

    public void generateCheckEmailToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUpAndCheckEmail() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
        this.nicknameLastChangedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canResendCheckEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean canChangeNickName() {
        return this.getNicknameLastChangedAt() == null
                || this.getNicknameLastChangedAt().isBefore(LocalDateTime.now().minusHours(24));
    }
}
