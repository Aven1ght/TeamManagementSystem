package ua.rud.teammanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.util.TimeZone;
@SpringBootApplication
@EntityScan("ua.rud.teammanagementsystem.entity")
public class TeamManagementSystemApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        SpringApplication.run(TeamManagementSystemApplication.class, args);

        //логування
        //кешування
        //підключити докер
    }

}
