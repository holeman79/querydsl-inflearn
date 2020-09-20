package com.example.querydsl;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslJoinTest {

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

    /**
     *  팀 A에 소속된 모든 회원
     */

    @Test
    @DisplayName("조인 테스트")
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
        assertThat(result.size()).isEqualTo(2);

    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    @DisplayName("세타조인 테스트 (카티션 곱)")
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     *
     * Left Join의 On조건에 and를 하고 조건을 걸 경우 Left Join 의 피조인 테이블의 데이터가 걸러진다!!
     * 즉 on절의 조건을 where절로 옮겼을 때 결과가 달라짐.
     * InnerJoin은 on절의 조건을 where절로 옮겼을 때 결과가 같다.
     * 몰랐던 부분이므로 꼭 잘 숙지하자!
     */
    @Test
    @DisplayName("Left Join 그리고 And 조건")
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은대상 외부 조인
     */
    @Test
    @DisplayName("연관관계가 없는 엔티티 조인 테스트")
    public void 연관관계_없는_조인(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("fetch_join_없을때")
    public void fetch_join_없을때(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("fetch_join_적용")
    public void fetch_join_적용(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }


}
