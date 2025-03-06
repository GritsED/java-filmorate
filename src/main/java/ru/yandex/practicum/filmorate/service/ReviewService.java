package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review createReview(Review review) {
        userStorage.findUser(review.getUserId());
        filmStorage.findFilm(review.getFilmId());
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Review with id = " + id + " not found"));
    }

    public List<Review> getReviewsToFilm(Long filmId, int count) {
        return reviewStorage.getReviewsToFilm(filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        return reviewStorage.getAllReviews(count);
    }

    public void likeReview(Long id, Long userId) {
        reviewStorage.likeDislike(id, userId, true);
    }

    public void dislikeReview(Long id, Long userId) {
        reviewStorage.likeDislike(id, userId, false);
    }

    public void deleteLike(Long id, Long userId) {
        reviewStorage.deleteLikeDislike(id, userId, true);
    }

    public void deleteDislike(Long id, Long userId) {
        reviewStorage.deleteLikeDislike(id, userId, false);
    }
}