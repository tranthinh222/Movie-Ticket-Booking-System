package com.cinema.ticketbooking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named="PG_TEST_URL", matches=".+")
class PostgreSqlSchemaTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){
  r.add("spring.datasource.url",()->System.getenv("PG_TEST_URL"));
  r.add("spring.datasource.username",()->System.getenv("PG_TEST_USER"));
  r.add("spring.datasource.password",()->System.getenv().getOrDefault("PG_TEST_PASSWORD",""));
  r.add("spring.datasource.driver-class-name",()->"org.postgresql.Driver");
  r.add("spring.jpa.hibernate.ddl-auto",()->"create-drop");
 }
 @Autowired EntityManager em;
 @Test void createsSchemaAndPersistsLongText(){
  var film=new com.cinema.ticketbooking.domain.Film();film.setName("PostgreSQL");
  film.setDescription("Nội dung phim ".repeat(1000));em.persist(film);em.flush();em.clear();
  assertEquals(film.getDescription(),em.find(com.cinema.ticketbooking.domain.Film.class,film.getId()).getDescription());
  assertTrue(em.createNativeQuery("select version()").getSingleResult().toString().contains("PostgreSQL"));
 }
}
