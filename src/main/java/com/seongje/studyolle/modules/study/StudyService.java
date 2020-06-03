package com.seongje.studyolle.modules.study;

import com.seongje.studyolle.domain.Tag;
import com.seongje.studyolle.domain.Zone;
import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.domain.study.StudyMember;
import com.seongje.studyolle.domain.study.StudyTagItem;
import com.seongje.studyolle.domain.study.StudyZoneItem;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.study.form.StudyDescriptionsForm;
import com.seongje.studyolle.modules.study.repository.StudyMemberRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import com.seongje.studyolle.modules.study.repository.StudyTagItemRepository;
import com.seongje.studyolle.modules.study.repository.StudyZoneItemRepository;
import com.seongje.studyolle.modules.tag.TagRepository;
import com.seongje.studyolle.modules.zone.ZoneRepository;
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
import java.util.Set;
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
    private final TagRepository tagRepository;
    private final StudyTagItemRepository studyTagItemRepository;
    private final ZoneRepository zoneRepository;
    private final StudyZoneItemRepository studyZoneItemRepository;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;
    private final IDataUrlSerializer dataUrlSerializer;

    @Transactional
    public Study createNewStudy(Study newStudyInfo, Account account) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study newStudy = studyRepository.save(newStudyInfo);

        if (findAccount != null) {
            StudyMember newStudyManager = studyMemberRepository.save(createStudyMember(newStudy, findAccount, MANAGER));
            newStudy.addStudyMember(newStudyManager);

            return newStudy;
        }

        return null;
    }

    public Study findStudy(String path) {
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalArgumentException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        return findStudy;
    }

    public Study findStudyForUpdate(String path, Account account) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalArgumentException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        if (findAccount != null) {
            if (!findStudy.isManagerBy(account)) {
                throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
            }

            return findStudy;
        }

        return null;
    }

    @Transactional
    public void joinToStudy(Account account, Study study) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study findStudy = studyRepository.findByPath(study.getPath());

        StudyMember newStudyMember = studyMemberRepository.save(createStudyMember(findStudy, findAccount, MEMBER));
        findStudy.addStudyMember(newStudyMember);
    }

    @Transactional
    public void leaveFromStudy(Account account, Study study) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study findStudy = studyRepository.findByPath(study.getPath());

        if (!studyMemberRepository.existsByAccountAndStudy(findAccount, findStudy)) {
            throw new IllegalArgumentException("스터디에서 해당 사용자를 찾을 수 없습니다.");
        }

        studyMemberRepository.deleteByAccountAndStudy(findAccount, findStudy);

        findStudy.removeStudyMember(findAccount);
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

    public Set<String> getStudyTags(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Set<StudyTagItem> studyTagItems = findStudy.getTags();

        return studyTagItems.stream().map(studyTagItem -> studyTagItem.getTag().getTitle()).collect(Collectors.toSet());
    }

    @Transactional
    public void addStudyTag(Study study, Tag tag) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Tag findTag = tagRepository.findByTitle(tag.getTitle());

        StudyTagItem newStudyTagItem = studyTagItemRepository.save(StudyTagItem.createStudyTagItem(findStudy, findTag));
        findStudy.addTagItem(newStudyTagItem);
    }

    @Transactional
    public void removeStudyTag(Study study, Tag tag) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Tag findTag = tagRepository.findByTitle(tag.getTitle());

        findStudy.removeTagItem(findTag);
    }

    public Set<String> getStudyZones(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Set<StudyZoneItem> zones = findStudy.getZones();

        return zones.stream().map(studyZoneItem -> studyZoneItem.getZone().toString()).collect(Collectors.toSet());
    }

    @Transactional
    public void addStudyZone(Study study, Zone zone) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Zone findZone = zoneRepository.findByCityAndLocalNameOfCity(zone.getCity(), zone.getLocalNameOfCity());

        StudyZoneItem newStudyZoneItem = studyZoneItemRepository.save(StudyZoneItem.createStudyZoneItem(findStudy, findZone));
        findStudy.addZoneItem(newStudyZoneItem);
    }

    @Transactional
    public void removeStudyZone(Study study, Zone zone) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        Zone findZone = zoneRepository.findByCityAndLocalNameOfCity(zone.getCity(), zone.getLocalNameOfCity());

        findStudy.removeZoneItem(findZone);
    }

    @Transactional
    public void studyPublish(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.publish();
    }

    @Transactional
    public void studyClose(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.close();
    }

    @Transactional
    public void studyRecruitStart(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.startRecruiting();
    }

    @Transactional
    public void studyRecruitStop(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.stopRecruiting();
    }

    @Transactional
    public boolean studyTitleUpdate(Study study, String newTitle) {
        if (newTitle.length() > 50) {
            return false;
        }

        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.setTitle(newTitle);

        return true;
    }

    @Transactional
    public boolean studyPathUpdate(Study study, String newPath) {
        String regex = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$";

        if (!newPath.matches(regex) || studyRepository.existsByPath(newPath)) {
            return false;
        }

        Study findStudy = studyRepository.findByPath(study.getPath());
        findStudy.setPath(newPath);

        return true;
    }

    @Transactional
    public void studyDelete(Study study) {
        Study findStudy = studyRepository.findByPath(study.getPath());

        if (!findStudy.canDeleteStudy()) {
            throw new IllegalArgumentException("해당 스터디를 삭제할 수 없습니다.");
        }

        studyRepository.delete(findStudy);
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
