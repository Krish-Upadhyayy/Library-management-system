package com.library.service;
import com.library.dao.MemberDAO;
import com.library.model.Member;
import java.util.List;

public class MemberService {

    private MemberDAO memberDAO;

    public MemberService() {
        memberDAO = new MemberDAO();
    }

    public void addMember(Member member) {
        memberDAO.addMember(member);
    }

    public List<Member> getAllMembers() {
        return memberDAO.getAllMembers();
    }

    public Member getMemberById(int id) {
        return memberDAO.getMemberById(id);
    }

    public void updateMember(Member member) {
        memberDAO.updateMember(member);
    }

    public void deleteMember(int id) {
        memberDAO.deleteMemberById(id);
    }
}