package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.domain.Comment
import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.repository.CommentRepository
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.repository.TagRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
    service: PostService,
) : BehaviorSpec({
    beforeSpec {
        postRepository.saveAll(
            listOf(
                Post("title1", "content1", "ben1"),
                Post("title12", "content1", "ben1"),
                Post("title13", "content1", "ben1"),
                Post("title14", "content1", "ben1"),
                Post("title15", "content1", "ben1"),
                Post("title6", "content1", "ben2"),
                Post("title7", "content1", "ben2"),
                Post("title8", "content1", "ben2"),
                Post("title9", "content1", "ben2"),
                Post("title0", "content1", "ben2")
            )
        )
    }

    given("게시글 생성 시") {
        When("게시글 인풋이 정상적으로 들어오면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "ben"
                )
            )
            then("게시글이 정상적으로 생성됨을 확인한다.") {
                postId shouldBeGreaterThan 0L
                val post = postRepository.findByIdOrNull(postId)
                post shouldNotBe null
                post?.title shouldBe "제목"
                post?.content shouldBe "내용"
                post?.createdBy shouldBe "ben"
            }
        }
        When("태그가 추가되면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "ben",
                    tags = listOf("tag1", "tag2")
                )
            )
            then("태그가 정상적으로 추가됨을 확인한다.") {
                val tags = tagRepository.findByPostId(postId)
                tags.size shouldBe 2
                tags[0].name shouldBe "tag1"
                tags[1].name shouldBe "tag2"
            }
        }
    }
    given("게시글 수정시") {
        val saved = postRepository.save(
            Post(
                title = "title",
                content = "content",
                createdBy = "ben",
                tags = listOf("tag1", "tag2")
            )
        )
        When("정상 수정시") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated_title",
                    content = "updated_content",
                    updatedBy = "ben"
                )
            )
            then("게시글이 정상적으로 수정됨을 확인한다.") {
                saved.id shouldBe updatedId
                val updatedPost = postRepository.findByIdOrNull(updatedId)
                updatedPost shouldNotBe null
                updatedPost?.title shouldBe "updated_title"
                updatedPost?.content shouldBe "updated_content"
                updatedPost?.updatedBy shouldBe "ben"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다라는 예외가 발생한다.") {
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(
                        -1L,
                        PostUpdateRequestDto(
                            title = "updated_title",
                            content = "updated_content",
                            updatedBy = "updated_ben"
                        )
                    )
                }
            }
        }

        When("작성자가 동일하지 않으면") {

            then("수정할 수 없는 게시물 입니다 예외가 발생한다.") {
                shouldThrow<PostNotUpdatableException> {
                    postService.updatePost(
                        1L,
                        PostUpdateRequestDto(
                            title = "updated_title",
                            content = "updated_content",
                            updatedBy = "updated_ben"
                        )
                    )
                }
            }
        }
        When("태그가 수정되었을 때") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated_title",
                    content = "updated_content",
                    updatedBy = "ben",
                    tags = listOf("tag1", "tag2", "tag3")
                )
            )
            then("정상적으로 수정됨을 확인한다.") {
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag3"
            }
            then("태그 순서가 변경되었을 때 정상적으로 변경됨을 확인한다.") {
                val updatedId = postService.updatePost(
                    saved.id,
                    PostUpdateRequestDto(
                        title = "updated_title",
                        content = "updated_content",
                        updatedBy = "ben",
                        tags = listOf("tag3", "tag2", "tag1")
                    )
                )
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag1"
            }
        }
    }
    given("게시글 삭제시") {
        val saved = postRepository.save(Post("title", "content", "ben"))
        When("정상 삭제시") {
            val postId = postService.deletePost(saved.id, "ben")
            then("게시글이 정상적으로 삭제됨을 확인한다.") {
                postId shouldBe saved.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }
        When("작성자가 동일하지 않으면") {
            val saved2 = postRepository.save(Post("title", "content", "ben"))
            then("삭제할 수 없는 게시글 입니다 예외가 발생한다.") {
                shouldThrow<PostNotDeletableException> {
                    postService.deletePost(saved2.id, "ben2")
                }
            }
        }
    }
    given("게시글 상세 조회시 ") {
        val saved = postRepository.save(Post("title", "content", "ben"))
        When("정상 조회시") {
            val post = postService.getPost(saved.id)
            then("게시글의 내용이 정상적으로 반환됨을 확인한다.") {
                post.id shouldBe saved.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "ben"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다라는 예외가 발생한다.") {
                shouldThrow<PostNotFoundException> {
                    postService.getPost(9999L)
                }
            }
        }
        When("댓글 추가시") {
            commentRepository.save(
                Comment(
                    "댓글 내용1",
                    saved,
                    "댓글 작성자"
                )
            )
            commentRepository.save(
                Comment(
                    "댓글 내용2",
                    saved,
                    "댓글 작성자"
                )
            )
            commentRepository.save(
                Comment(
                    "댓글 내용3",
                    saved,
                    "댓글 작성자"
                )
            )
            val post = postService.getPost(saved.id)
            then("댓글이 함께 조회됨을 확인한다.") {
                post.comments.size shouldBe 3
                post.comments[0].content shouldBe "댓글 내용1"
                post.comments[1].content shouldBe "댓글 내용2"
                post.comments[2].content shouldBe "댓글 내용3"
                post.comments[0].createdBy shouldBe "댓글 작성자"
                post.comments[1].createdBy shouldBe "댓글 작성자"
                post.comments[2].createdBy shouldBe "댓글 작성자"
            }
        }
    }
    given("게시글 목록 조회시") {
        When("정상 조회시") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto())
            then("게시글 페이지가 반환된다.") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title"
                postPage.content[0].createdBy shouldContain "ben"
            }
        }
        When("타이틀로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(title = "title1"))
            then("타이틀에 해당하는 게시글이 반환된다.") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title1"
                postPage.content[0].createdBy shouldContain "ben1"
            }
        }
        When("작성자로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(createdBy = "ben1"))
            then("작성자에 해당하는 게시글이 반환된다.") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title1"
                postPage.content[0].createdBy shouldBe "ben1"
            }
        }
    }
})
