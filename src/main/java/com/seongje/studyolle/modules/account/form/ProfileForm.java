package com.seongje.studyolle.modules.account.form;

import com.seongje.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class ProfileForm {

    @Length(max = 35)
    private String aboutMe;

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String occupation;

    @Length(max = 50)
    private String location;

    private String profileImg;

    public ProfileForm(Account account) {
        this.aboutMe = account.getAboutMe();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImg = account.getProfileImg();
    }
}
