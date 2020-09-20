package com.example.querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
public class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity(){
        Team teamA = makeTeam("teamA");
        Team teamB = makeTeam("teamB");
        Member member1 = makeMember("member1", 10, teamA);
        Member member2 = makeMember("member2", 20, teamA);
        Member member3 = makeMember("member3", 30, teamB);
        Member member4 = makeMember("member4", 40, teamB);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        for(Member member : members){
            System.out.println("Member : " + member);
            System.out.println("-> member.team : " + member.getTeam() );
        }
    }

    private Member makeMember(String name, int age, Team team) {
        Member member = new Member(name, age, team);
        em.persist(member);
        return member;
    }

    private Team makeTeam(String name) {
        Team team = new Team(name);
        em.persist(team);
        return team;
    }
}