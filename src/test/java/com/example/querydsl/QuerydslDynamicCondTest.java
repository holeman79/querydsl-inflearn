package com.example.querydsl;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslDynamicCondTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = makeTeam("teamA");
        Team teamB = makeTeam("teamB");
        Member member1 = makeMember("member1", 10, teamA);
        Member member2 = makeMember("member2", 20, teamA);
        Member member3 = makeMember("member3", 30, teamB);
        Member member4 = makeMember("member4", 40, teamB);
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

    @Test
    public void 동적쿼리_BooleanBuilder(){
        String username = "member1";
        Integer ageParam = 10;

        List<Member> members = searchMember1(username, ageParam);
        assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void 동적쿼리_whereParam(){
        String username = "member1";
        Integer ageParam = 10;

        List<Member> members = searchMember2(username, ageParam);
        assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                //.where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


}
