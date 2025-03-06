package com.example.backend.domain.group.service;

import com.example.backend.domain.category.entity.Category;
import com.example.backend.domain.category.repository.CategoryRepository
import com.example.backend.domain.group.dto.GroupLocationDto
import com.example.backend.domain.group.dto.GroupModifyRequestDto
import com.example.backend.domain.group.dto.GroupRequestDto
import com.example.backend.domain.group.dto.GroupResponseDto;
import com.example.backend.domain.group.entity.Group;
import com.example.backend.domain.group.entity.GroupStatus;
import com.example.backend.domain.group.exception.GroupErrorCode;
import com.example.backend.domain.group.exception.GroupException;
import com.example.backend.domain.group.repository.GroupRepository
import com.example.backend.domain.groupcategory.GroupCategory;
import com.example.backend.domain.groupmember.entity.GroupMember;
import com.example.backend.domain.groupmember.entity.GroupMemberStatus
import com.example.backend.domain.groupmember.repository.GroupMemberRepository
import com.example.backend.domain.member.entity.Member;
import com.example.backend.domain.member.repository.MemberRepository
import com.example.backend.domain.vote.repository.VoteRepository
import com.example.backend.domain.voter.repository.VoterRepository
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
class GroupService(
    val groupRepository : GroupRepository,
    val memberRepository : MemberRepository,
    val groupMemberRepository : GroupMemberRepository,
    val categoryRepository : CategoryRepository,
    val voteRepository : VoteRepository,
    val voterRepository : VoterRepository
) {
    @Transactional
    fun create(groupRequestDto : GroupRequestDto, id : Long) : GroupResponseDto{

        val member : Member = memberRepository.findById(id).orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND_MEMBER)}

        val categories : List<Category> = categoryRepository.findAllById(groupRequestDto.categoryIds)
        val group : Group = Group(
                groupRequestDto.title,
                groupRequestDto.description,
                member,
                groupRequestDto.status,
                groupRequestDto.maxParticipants
        );

        val groupCategories : MutableList<GroupCategory> = categories.map { category -> GroupCategory(group, category) }.toMutableList()

        group.addGroupCategories(groupCategories);
        groupRepository.save(group);
        val groupMember = GroupMember(member, group, GroupMemberStatus.APPLYING)
        groupMemberRepository.save(groupMember);

        return GroupResponseDto(group);
    }

    @Transactional(readOnly = true)
    fun findAllGroups() : List<GroupResponseDto>  {
        val groups : List<GroupResponseDto> = groupRepository.findAll().map{GroupResponseDto(it)}
        if (groups.isEmpty()) {
            throw GroupException(GroupErrorCode.NOT_FOUND_LIST);
        }
        return groups
    }

    @Transactional(readOnly = true)
    fun findGroup(id : Long) : GroupResponseDto{
        return groupRepository.findById(id).map{GroupResponseDto(it)}.orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND)}
    }


    @Transactional
    fun deleteGroup(id : Long) {
        val group : Group = groupRepository.findById(id).orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND)}
        checkValidity(group.status)
		if (group.status == GroupStatus.COMPLETED){
			group.updateStatus(GroupStatus.DELETED)
		}
        group.updateStatus(GroupStatus.DELETED)

		val voteIds : List<Long> = voteRepository.findAllIdByGroupId(id);
		voterRepository.deleteByVoteIdIn(voteIds);
		voteRepository.deleteAllByGroupId(id);
        groupRepository.save(group);
    }

    @Transactional
    fun modifyGroup(id : Long, groupModifyRequestDto : GroupModifyRequestDto) : GroupResponseDto {
        val group : Group = groupRepository.findById(id).orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND)}
        checkValidity(group.status);
        group.update(
                groupModifyRequestDto.title,
                groupModifyRequestDto.description,
                groupModifyRequestDto.maxParticipants,
                groupModifyRequestDto.groupStatus
        );
        groupRepository.save(group);
        return GroupResponseDto(group);
    }

    fun checkValidity(groupStatus : GroupStatus){
        when(groupStatus){
            GroupStatus.DELETED -> { throw GroupException(GroupErrorCode.ALREADY_DELETED)}
            GroupStatus.COMPLETED -> { throw GroupException(GroupErrorCode.COMPLETED)}
            GroupStatus.VOTING -> { throw GroupException(GroupErrorCode.VOTING)}
            else -> {}
        }
    }

    @Transactional
    fun joinGroup(groupId : Long, memberId : Long) {
        val group : Group = groupRepository.findById(groupId).orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND)}

        val member : Member = memberRepository.findById(memberId).orElseThrow{throw GroupException(GroupErrorCode.NOT_FOUND_MEMBER)}

        if (groupMemberRepository.existsByGroupAndMember(group, member)) {
            throw  GroupException(GroupErrorCode.EXISTED_MEMBER)
        }

        val currentMember : Long = groupMemberRepository.countByGroup(group)
        if (currentMember > group.maxParticipants-1) {
            throw GroupException(GroupErrorCode.OVER_MEMBER)
        }

        val groupMember = GroupMember(member, group)

        groupMemberRepository.save(groupMember)
    }

    @Transactional(readOnly = true)
    fun getGroupsByMemberId(memberId : Long) : List<GroupResponseDto>{
        val groups : List<Group> = groupRepository.findGroupByMemberId(memberId)
        return groups.map{GroupResponseDto(it)}
    }

    @Transactional(readOnly = true)
    fun findNotDeletedAllGroups() : List<GroupResponseDto> {
        val groups = groupRepository.findAll()
            .filter { it.status != GroupStatus.DELETED && it.status != GroupStatus.NOT_RECRUITING }
            .map { GroupResponseDto(it) }

        if (groups.isEmpty()) {
            throw GroupException(GroupErrorCode.NOT_FOUND_LIST)
        }
        return groups
    }

    @Transactional(readOnly = true)
    fun getLocationOfCompletedGroup(memberId: Long): List<GroupLocationDto> {
        val groups : List<Group> = groupRepository.findCompletedGroupsByMemberId(memberId)
        if (groups.isEmpty()) {
            throw GroupException(GroupErrorCode.NOT_FOUND_LIST)
        }
        return groups.map { group ->
            val topLocation = voteRepository.findTopVotedLocationByGroupId(group.id)
            if (topLocation == null){
                throw GroupException(GroupErrorCode.NOT_FOUND_LOCATION)
            }
            GroupLocationDto(group.id,group.title, topLocation.toString())
        }
    }
}

