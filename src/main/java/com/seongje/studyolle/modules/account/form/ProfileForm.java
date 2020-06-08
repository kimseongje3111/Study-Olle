package com.seongje.studyolle.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Data
public class ProfileForm {

    @Length(max = 35)
    private String aboutMe;

    @Length(max = 50)
    @URL
    private String url;

    @Length(max = 50)
    private String occupation;

    @Length(max = 50)
    private String location;

    private String profileImg;
}
