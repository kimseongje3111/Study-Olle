package com.seongje.studyolle.modules.study;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.domain.study.StudyMember;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.study.form.StudyDescriptionsForm;
import com.seongje.studyolle.modules.study.repository.StudyMemberRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlBuilder;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.seongje.studyolle.domain.study.ManagementLevel.*;
import static com.seongje.studyolle.domain.study.StudyMember.*;
import static eu.maxschuster.dataurl.DataUrlEncoding.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;
    private final IDataUrlSerializer dataUrlSerializer;

    @Transactional
    public Study createNewStudy(Study newStudyInfo, Account account) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study newStudy = studyRepository.save(newStudyInfo);

        if (findAccount != null) {
            StudyMember newStudyMember = studyMemberRepository.save(createStudyMember(newStudy, findAccount, MANAGER));
            newStudy.addStudyMember(newStudyMember);

            return newStudy;
        }

        return null;
    }

    public Study findStudy(String path) {
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalStateException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        return findStudy;
    }

    public Study findStudyForUpdate(String path, Account account) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalStateException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        if (findAccount != null) {
            if (!findStudy.isManagerBy(account)) {
                throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
            }

            return findStudy;
        }

        return null;
    }

    public List<String> getBasicBannerImages() {
        try {
            Resource[] resources = ResourcePatternUtils
                    .getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:/static/images/basic_banners/**");

            return Arrays.stream(resources).map(Resource::getFilename).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("해당 경로에 파일이 존재하지 않습니다.", e);
        }
    }

    @Transactional
    public void updateDescriptions(Study study, StudyDescriptionsForm studyDescriptionsForm) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        modelMapper.map(studyDescriptionsForm, findStudy);
    }

    @Transactional
    public void updateBannerImage(Study study, String studyBannerImage) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.setBannerImg(studyBannerImage);
    }

    @Transactional
    public void updateBannerImageByBasic(Study study, String basicBanner) {
        Study findStudy = studyRepository.findByPath(study.getPath());

        try {
            findStudy.setBannerImg(getImageDataUrl(basicBanner));

        } catch (IOException e) {
            throw new RuntimeException("파일을 찾을 수 없거나 이미지를 불러올 수 없습니다.");
        }
    }

    @Transactional
    public void useBannerImage(Study study, boolean use) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.setUseBanner(use);

        if (!use) {
            findStudy.setBannerImg(null);
        }
    }

    private String getImageDataUrl(String basicBanner) throws IOException {
        Resource dataSource = new ClassPathResource("static/images/basic_banners/" + basicBanner);
        byte[] bytes = Files.readAllBytes(dataSource.getFile().toPath());

        DataUrl dataUrl = new DataUrlBuilder()
                .setMimeType("image/png")
                .setEncoding(BASE64)
                .setData(bytes)
                .build();

        return dataUrlSerializer.serialize(dataUrl);
    }
}
