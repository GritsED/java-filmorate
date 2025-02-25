package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.FriendDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FriendDbStorage friendDbStorage;
    User user;
    User user2;
    User user3;

    @Autowired
    public FilmorateApplicationTests(UserDbStorage userStorage, FriendDbStorage friendDbStorage) {
        this.userStorage = userStorage;
        this.friendDbStorage = friendDbStorage;
    }

    @BeforeEach
    public void init() {
        user = User.builder()
                .name("Tim")
                .email("weg@ya.ru")
                .login("Log")
                .birthday(LocalDate.now())
                .build();

        user2 = User.builder()
                .name("Leo")
                .email("weg12@ya.ru")
                .login("Goal")
                .birthday(LocalDate.now())
                .build();

        user3 = User.builder()
                .email("3@ya.ru")
                .login("User3")
                .birthday(LocalDate.now())
                .build();
    }

    @Test
    public void testCreateUser() {
        User createUser = userStorage.create(user);

        assertThat(createUser.getId()).isEqualTo(1);
        assertThat(createUser.getName()).isEqualTo("Tim");
    }

    @Test
    public void testFindUserById() {
        User createUser = userStorage.create(user);

        User foundUser = userStorage.findUser(createUser.getId());

        assertThat(foundUser.getId()).isEqualTo(1);
        assertThat(foundUser.getName()).isEqualTo("Tim");
    }

    @Test
    public void testFindAllUser() {
        List<User> createUser = List.of(user, user2);
        createUser.forEach(userStorage::create);

        List<User> users = userStorage.findAll().stream().toList();

        assertThat(users).hasSize(createUser.size());
    }

    @Test
    public void testUpdateUser() {
        userStorage.create(user);

        User updateUser = User.builder()
                .id(1L)
                .email("qwer@ya.ru")
                .login("UpdateLog")
                .name("UpdateName")
                .birthday(LocalDate.now()).build();

        userStorage.updateUser(updateUser);
        User actualUser = userStorage.findUser(1L);

        assertThat(actualUser.getId()).isEqualTo(1L);
        assertThat(actualUser.getName()).isEqualTo(updateUser.getName());
    }

    @Test
    public void testRemoveUser() {
        userStorage.create(user);
        userStorage.create(user2);

        userStorage.removeUser(user2.getId());

        List<User> users = userStorage.findAll().stream().toList();

        assertThat(users).hasSize(1);
    }

    @Test
    public void testAddFriend() {
        userStorage.create(user);
        userStorage.create(user2);

        friendDbStorage.addFriend(user.getId(), user2.getId());

        assertThat(userStorage.findUser(user.getId()).getFriends().size()).isEqualTo(1);
        assertThat(userStorage.findUser(user2.getId()).getFriends().size()).isEqualTo(0);
    }

    @Test
    public void testRemoveFriend() {
        userStorage.create(user);
        userStorage.create(user2);
        friendDbStorage.addFriend(user.getId(), user2.getId());

        assertThat(userStorage.findUser(user.getId()).getFriends().size()).isEqualTo(1);

        friendDbStorage.removeFriend(user.getId(), user2.getId());
        assertThat(userStorage.findUser(user.getId()).getFriends().size()).isEqualTo(0);
    }

    @Test
    public void testGetUserFriends() {
        userStorage.create(user);
        userStorage.create(user2);
        friendDbStorage.addFriend(user.getId(), user2.getId());

        List<User> friends = friendDbStorage.getUserFriends(user.getId()).stream().toList();

        assertThat(friends).hasSize(1);
        assertThat(friends.getFirst().getName()).isEqualTo(user2.getName());
    }

    @Test
    public void testGetCommonFriends() {
        userStorage.create(user);
        userStorage.create(user2);
        userStorage.create(user3);
        friendDbStorage.addFriend(user.getId(), user3.getId());
        friendDbStorage.addFriend(user2.getId(), user3.getId());

        List<User> friends = friendDbStorage.getCommonFriends(user.getId(), user2.getId())
                .stream().toList();

        assertThat(friends).hasSize(1);
        assertThat(friends.getFirst().getName()).isEqualTo(user3.getName());
    }
}
