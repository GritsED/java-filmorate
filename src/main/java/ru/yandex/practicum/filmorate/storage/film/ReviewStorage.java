package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long id);

    Optional<Review> getReviewById(Long id);

    List<Review> getReviewsToFilm(Long filmId, int count);

    List<Review> getAllReviews(int count);

    void likeDislike(Long reviewId, Long userId, boolean isPositive);

    void deleteLikeDislike(Long reviewId, Long userId, boolean isPositive);
}
