package com.seongje.studyolle.modules.tag;

import com.seongje.studyolle.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag findOrCreateTag(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);

        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }

        return tag;
    }

    public Tag findByTagTitle(String tagTitle) {
        return tagRepository.findByTitle(tagTitle);
    }

    public List<String> getAllTags() {
        return tagRepository.findAll().stream().map(Tag::getTitle).sorted().collect(Collectors.toList());
    }
}
