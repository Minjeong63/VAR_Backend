package shootingstar.var.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shootingstar.var.Service.UserService;
import shootingstar.var.Service.dto.FollowingDto;
import shootingstar.var.Service.dto.UserProfileDto;
import shootingstar.var.Service.dto.UserSignupReqDto;
import shootingstar.var.entity.Follow;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.FollowRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupReqDto reqDto) {

        userService.signup(reqDto);

        return ResponseEntity.ok().body("회원가입 성공");
    }

    @GetMapping("/duplicate/{nickname}")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.checkNicknameDuplicate(nickname));
    }

    @GetMapping("/profile/{nickname}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String nickname) {
        UserProfileDto profile = userService.getProfile(nickname);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/checkVIP/{nickname}")
    public ResponseEntity<Boolean> checkVIP(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(userService.checkVIP(nickname));
    }

    @GetMapping("/followingList")
    public ResponseEntity<?> followingList(@RequestParam("nickname") String nickname) {
        List<FollowingDto> followingList = userService.findAllFollowing(nickname);
        return ResponseEntity.ok().body(followingList);
    }
    @PostMapping("/follow/{followingId}")
    public ResponseEntity<String> follow(@PathVariable String followingId, HttpServletRequest request) {
        String accessToken = getTokenFromHeader(request);
        userService.follow(followingId,accessToken );
        return ResponseEntity.ok("follow success");
    }
    @DeleteMapping("/unfollow/{followUUID}")
    public ResponseEntity<String> unFollow(@PathVariable("followingId") String followUUID) {
        userService.unFollow(followUUID);
        return ResponseEntity.ok().body("unfollow success");
    }


    @GetMapping("/test")
    public String test() {
        return "접근 성공";
    }

    private static String getTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader("Authorization"); // 헤더에 존재하는 엑세스 토큰을 받아온다.

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 접두어 제거
        } else {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return token;
    }
}
