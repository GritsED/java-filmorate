package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    Long id;
    @Email(message = "Uncorrected email. Please use a valid format like example@domain.com")
    String email;
    @NotBlank(message = "Login cannot be empty or contain spaces")
    String login;
    String name;
    @NotNull(message = "Date of birth cannot be null.")
    @Past(message = "Date of birth cannot be in the future.")
    LocalDate birthday;
    Set<Long> friends;

    public void addFriend(Long friendId) {
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add(friendId);
    }

    public boolean removeFriend(Long friendId) {
        return friends.remove(friendId);
    }
}