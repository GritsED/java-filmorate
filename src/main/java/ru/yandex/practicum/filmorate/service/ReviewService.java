package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
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
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventService = eventService;
    }

    public Review createReview(Review review) {
        userStorage.findUser(review.getUserId());
        filmStorage.findFilm(review.getFilmId());
        Review result = reviewStorage.createReview(review);
        eventService.add(result.getReviewId(), review.getUserId(), EventType.REVIEW, Operation.ADD);
        return result;
    }

    public Review updateReview(Review review) {
        Review result = reviewStorage.updateReview(review);
        eventService.add(result.getReviewId(), result.getUserId(), EventType.REVIEW, Operation.UPDATE);
        return result;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        eventService.add(review.getReviewId(), review.getUserId(), EventType.REVIEW, Operation.REMOVE);
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