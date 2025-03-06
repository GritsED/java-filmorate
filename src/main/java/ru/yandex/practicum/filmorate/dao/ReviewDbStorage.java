package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private static final String INSERT_REVIEW = """
            INSERT INTO reviews (content, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_REVIEW = """
            UPDATE reviews SET content = ?, is_positive = ?, film_id = ?
            WHERE id = ?
            """;
    private static final String DELETE_REVIEW = """
            DELETE FROM reviews
            WHERE id = ?
            """;
    private static final String DELETE_USEFUL = """
            DELETE FROM useful
            WHERE review_id = ?
            """;
    private static final String FIND_REVIEW_BY_ID = """
            SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,
            COALESCE(SUM(CASE WHEN uf.is_like IS TRUE THEN 1 ELSE 0 END), 0) AS likes,
            COALESCE(SUM(CASE WHEN uf.is_like IS FALSE THEN 1 ELSE 0 END), 0) AS dislikes
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            JOIN films f ON r.film_id = f.id
            LEFT JOIN useful uf ON r.id = uf.review_id
            LEFT JOIN useful u_like ON r.id = u_like.review_id
            WHERE r.id = ?
            GROUP BY r.id, r.content, r.is_positive, u.name, f.name, r.user_id, r.film_id
            """;
    private static final String FIND_REVIEWS_TO_FILM = """
            SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,
            COALESCE(SUM(CASE WHEN uf.is_like IS TRUE THEN 1 ELSE 0 END), 0) AS likes,
            COALESCE(SUM(CASE WHEN uf.is_like IS FALSE THEN 1 ELSE 0 END), 0) AS dislikes
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            JOIN films f ON r.film_id = f.id
            LEFT JOIN useful uf ON r.id = uf.review_id
            LEFT JOIN useful u_like ON r.id = u_like.review_id
            WHERE r.film_id = ?
            GROUP BY r.id, r.content, r.is_positive, u.name, f.name, r.user_id, r.film_id
            LIMIT ?
            """;
    private static final String GET_ALL_REVIEWS = """
            SELECT *
            FROM reviews
            LIMIT ?
            """;
    private static final String INSERT_LIKE_DISLIKE = """
            INSERT INTO useful (review_id, user_id, is_like)
            VALUES (?, ?, ?)
            """;
    private static final String DELETE_LIKE_DISLIKE = """
            DELETE
            FROM useful WHERE review_id = ?
            AND user_id = ?
            """;
    private static final String CHECK_USEFUL = """
            SELECT *
            FROM useful
            WHERE review_id = ?
            AND user_id = ?
            """;
    private static final String UPDATE_USEFUL = """
            UPDATE useful
            SET is_like = ?
            WHERE review_id = ?
            AND user_id = ?
            """;
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    public Review createReview(Review review) {
        log.debug("Received request to create a new review: {}", review);
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, key);
        Long id = Objects.requireNonNull(key.getKey()).longValue();
        review.setReviewId(id);
        log.debug("Review successfully added with ID {}", review.getReviewId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        log.debug("Received request to update review with ID: {}", review.getReviewId());
        if (review.getReviewId() == null) {
            log.warn("Failed to update review: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getFilmId(),
                review.getReviewId()
        );
        if (update == 0) {
            log.warn("Review update failed: Review with ID {} not found", review.getReviewId());
            throw new NotFoundException("Review with id  = " + review.getReviewId() + " not found.");
        }
        log.debug("Review with ID {} successfully updated", review.getReviewId());
        return getReviewById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review with id = " + review.getReviewId() + " not found"));
    }

    @Override
    public void deleteReview(Long id) {
        log.debug("Received request to remove review with ID: {}", id);
        jdbc.update(DELETE_USEFUL, id);
        jdbc.update(DELETE_REVIEW, id);
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        log.debug("Received request to find review with ID {}", id);
        try {
            Review review = jdbc.queryForObject(FIND_REVIEW_BY_ID, reviewRowMapper, id);
            log.debug("Returning review details for ID {}", id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Review with ID {} not found", id);
            throw new NotFoundException("Review with id " + id + " not found");
        }
    }

    @Override
    public List<Review> getReviewsToFilm(Long filmId, int count) {
        log.debug("Received request to get review by film with ID {}", filmId);
        List<Review> reviews = jdbc.query(FIND_REVIEWS_TO_FILM, reviewRowMapper, filmId, count);
        log.debug("Returning list of reviews");
        return reviews;
    }

    @Override
    public List<Review> getAllReviews(int count) {
        log.debug("Received request to get all reviews");
        List<Review> reviews = jdbc.query(GET_ALL_REVIEWS, reviewRowMapper, count);
        log.debug("Returning list of all reviews");
        return reviews;
    }

    @Override
    public void likeDislike(Long reviewId, Long userId, boolean isLike) {
        List<Map<String, Object>> likeDislike = jdbc.queryForList(CHECK_USEFUL, reviewId, userId);
        if (!likeDislike.isEmpty()) {
            jdbc.update(UPDATE_USEFUL, isLike, reviewId, userId);
        } else {
            jdbc.update(INSERT_LIKE_DISLIKE, reviewId, userId, isLike);
        }
    }

    @Override
    public void deleteLikeDislike(Long reviewId, Long userId, boolean isLike) {
            jdbc.update(DELETE_LIKE_DISLIKE, reviewId, userId);
        }
    }