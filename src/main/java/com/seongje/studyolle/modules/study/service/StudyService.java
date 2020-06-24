package com.seongje.studyolle.modules.study.service;

import com.seongje.studyolle.modules.study.app_event.custom.*;
import com.seongje.studyolle.modules.study.domain.StudyMember;
import com.seongje.studyolle.modules.study.domain.StudyTagItem;
import com.seongje.studyolle.modules.study.domain.StudyZoneItem;
import com.seongje.studyolle.modules.study.form.StudySearch;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.tag.repository.TagRepository;
import com.seongje.studyolle.modules.zone.domain.Zone;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.form.StudyDescriptionsForm;
import com.seongje.studyolle.modules.study.repository.StudyMemberRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import com.seongje.studyolle.modules.study.repository.StudyTagItemRepository;
import com.seongje.studyolle.modules.study.repository.StudyZoneItemRepository;
import com.seongje.studyolle.modules.zone.repository.ZoneRepository;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlBuilder;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.seongje.studyolle.modules.study.app_event.custom.StudyMemberEventType.*;
import static com.seongje.studyolle.modules.study.app_event.custom.StudyUpdatedEventType.*;
import static com.seongje.studyolle.modules.study.domain.ManagementLevel.*;
import static com.seongje.studyolle.modules.study.domain.StudyMember.*;
import static com.seongje.studyolle.modules.study.domain.StudyTagItem.*;
import static com.seongje.studyolle.modules.study.domain.StudyZoneItem.*;
import static eu.maxschuster.dataurl.DataUrlEncoding.*;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyTagItemRepository studyTagItemRepository;
    private final StudyZoneItemRepository studyZoneItemRepository;
    private final ModelMapper modelMapper;
    private final ResourceLoader resourceLoader;
    private final IDataUrlSerializer dataUrlSerializer;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;

    public Study findStudy(String path) {
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalArgumentException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        return findStudy;
    }

    public Study findStudyForUpdate(String path, Account account) {
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) {
            throw new IllegalArgumentException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");
        }

        if (!findStudy.isManagerBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
        }

        return findStudy;
    }

    @Transactional
    public Study createNewStudy(Study newStudyInfo, Account account) {
        Study newStudy = studyRepository.save(newStudyInfo);

        newStudy.addStudyMember(
                studyMemberRepository.save(createStudyMember(newStudy, account, MANAGER))
        );

        return newStudy;
    }

    @Transactional
    public void joinToStudy(Study study, Account account) {
        study.addStudyMember(
                studyMemberRepository.save(createStudyMember(study, account, MEMBER))
        );

        eventPublisher.publishEvent(new StudyMemberEvent(study, account, JOIN));
    }

    @Transactional
    public void leaveFromStudy(Study study, Account account) {
        if (!studyMemberRepository.existsByAccountAndStudy(account, study)) {
            throw new IllegalArgumentException("스터디에서 해당 사용자를 찾을 수 없습니다.");
        }

        studyMemberRepository.deleteByAccountAndStudy(account, study);
        study.removeStudyMember(account);

        eventPublisher.publishEvent(new StudyMemberEvent(study, account, LEAVE));
    }

    public List<String> getBasicBannerImages() {
        try {
            Resource[] resources = ResourcePatternUtils
                    .getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:/static/images/basic_banners/**");

            return Arrays.stream(resources).map(Resource::getFilename).collect(toList());

        } catch (IOException e) {
            throw new RuntimeException("해당 경로에 파일이 존재하지 않습니다.", e);
        }
    }

    @Transactional
    public void updateDescriptions(Study study, StudyDescriptionsForm studyDescriptionsForm) {
        modelMapper.map(studyDescriptionsForm, study);
    }

    @Transactional
    public void updateBannerImage(Study study, String studyBannerImage) {
        study.setBannerImg(studyBannerImage);
    }

    @Transactional
    public void updateBannerImageByBasic(Study study, String basicBanner) {
        try {
            study.setBannerImg(getImageDataUrl(basicBanner));

        } catch (IOException e) {
            throw new RuntimeException("파일을 찾을 수 없거나 이미지를 불러올 수 없습니다.");
        }
    }

    @Transactional
    public void useBannerImage(Study study, boolean use) {
        if (!use) {
            study.setBannerImg(null);
        }

        study.setUseBanner(use);
    }

    public Set<Tag> getStudyTags(Study study) {
        return study.getTags().stream().map(StudyTagItem::getTag).collect(toSet());
    }

    @Transactional
    public void addStudyTag(Study study, Tag tag) {
        study.addTagItem(
                studyTagItemRepository.save(createStudyTagItem(study, tag))
        );
    }

    @Transactional
    public void removeStudyTag(Study study, Tag tag) {
        study.removeTagItem(tag);
    }

    public Set<Zone> getStudyZones(Study study) {
        return study.getZones().stream().map(StudyZoneItem::getZone).collect(toSet());
    }

    @Transactional
    public void addStudyZone(Study study, Zone zone) {
        study.addZoneItem(
                studyZoneItemRepository.save(createStudyZoneItem(study, zone))
        );
    }

    @Transactional
    public void removeStudyZone(Study study, Zone zone) {
        study.removeZoneItem(zone);
    }

    @Transactional
    public void studyPublish(Study study) {
        study.publish();

        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    @Transactional
    public void studyClose(Study study) {
        study.close();

        eventPublisher.publishEvent(new StudyUpdatedEvent(study, CLOSED));
    }

    @Transactional
    public void studyRecruitStart(Study study) {
        study.startRecruiting();

        eventPublisher.publishEvent(new StudyUpdatedEvent(study, RECRUIT_START));
    }

    @Transactional
    public void studyRecruitStop(Study study) {
        study.stopRecruiting();

        eventPublisher.publishEvent(new StudyUpdatedEvent(study, RECRUIT_STOP));
    }

    @Transactional
    public boolean studyTitleUpdate(Study study, String newTitle) {
        if (newTitle.length() > 50) {
            return false;
        }

        String prevTitle = study.getTitle();
        study.setTitle(newTitle);

        StudyUpdatedEvent studyUpdatedEvent = new StudyUpdatedEvent(study, TITLE);
        studyUpdatedEvent.setPrevTitle(prevTitle);
        eventPublisher.publishEvent(studyUpdatedEvent);

        return true;
    }

    @Transactional
    public boolean studyPathUpdate(Study study, String newPath) {
        String regex = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$";

        if (!newPath.matches(regex) || studyRepository.existsByPath(newPath)) {
            return false;
        }

        String prevPath = study.getPath();
        study.setPath(newPath);

        StudyUpdatedEvent studyUpdatedEvent = new StudyUpdatedEvent(study, PATH);
        studyUpdatedEvent.setPrevPath(prevPath);
        eventPublisher.publishEvent(studyUpdatedEvent);

        return true;
    }

    @Transactional
    public void studyDelete(Study study) {
        if (!study.canDeleteStudy()) {
            throw new IllegalArgumentException("해당 스터디를 삭제할 수 없습니다.");
        }

        studyRepository.delete(study);

        eventPublisher.publishEvent(new StudyDeletedEvent(study));
    }

    public List<Study> getUserStudiesForNotClosed(Account account) {
        List<StudyMember> userStudies =
                studyMemberRepository.searchAllByAccountAndPublishedDateTimeDesc(account.getId());

        return userStudies.stream()
                .map(StudyMember::getStudy)
                .filter(study -> !study.isClosed())
                .collect(toList());
    }

    public List<Study> getUserStudiesForClosed(Account account) {
        List<StudyMember> userStudies =
                studyMemberRepository.searchAllByAccountAndPublishedDateTimeDesc(account.getId());

        return userStudies.stream()
                .map(StudyMember::getStudy)
                .filter(study -> study.isPublished() && study.isClosed())
                .collect(toList());
    }

    public Page<Study> getAllStudiesForKeyword(StudySearch studySearch, Pageable pageable) {
        return studyRepository
                .searchAllByStudySearchAndPaging(studySearch, pageable);
    }

    public List<Study> getStudiesForRecent9() {
        return studyRepository
                .findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);
    }

    public List<Study> getRecommendedStudiesForTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        List<Study> studyList
                = studyRepository.searchPublishedAndNotClosedByTagsAndZonesAndPublishedDateTimeDesc(tags, zones);

        return studyList.size() > 10 ? studyList.subList(0, 9) : studyList;
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
