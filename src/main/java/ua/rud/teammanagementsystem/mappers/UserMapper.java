package ua.rud.teammanagementsystem.mappers;

import org.springframework.stereotype.Component;
import ua.rud.teammanagementsystem.entity.User;
import ua.rud.teammanagementsystem.responses.UserResponse;
@Component
public class UserMapper {
    public UserResponse mapTo(User user){
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
