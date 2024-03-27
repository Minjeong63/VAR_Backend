package shootingstar.var.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.entity.Auction;
import shootingstar.var.entity.AuctionType;
import shootingstar.var.entity.ScheduledTask;
import shootingstar.var.entity.TaskType;
import shootingstar.var.entity.User;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.quartz.TicketCreationJob;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.ScheduledTaskRepository;
import shootingstar.var.repository.UserRepository;
import shootingstar.var.dto.req.AuctionCreateReqDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final String JOB_GROUP_NAME = "ticket-creation-jobs";

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final Scheduler scheduler;

    @Transactional
    public void create(AuctionCreateReqDto reqDto, String userUUID) {
        User findUser = userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 보유 포인트보다 최소 입찰 금액이 더 클 경우
        if (findUser.getPoint() < reqDto.getMinBidAmount()) {
            throw new CustomException(ErrorCode.MIN_BID_AMOUNT_INCORRECT_FORMAT);
        }

        // 경매 생성
        Auction auction = Auction.builder()
                .user(findUser)
                .minBidAmount(reqDto.getMinBidAmount())
                .meetingDate(LocalDateTime.parse(reqDto.getMeetingDate()))
                .meetingLocation(reqDto.getMeetingLocation())
                .meetingInfoText(reqDto.getMeetingInfoText())
                .meetingPromiseText(reqDto.getMeetingPromiseText())
                .meetingInfoImg(reqDto.getMeetingInfoImg())
                .meetingPromiseImg(reqDto.getMeetingPromiseImg())
                .build();

        auctionRepository.save(auction);

        // 포인트 차감
        findUser.decreasePoint(auction.getMinBidAmount());

        // 스케줄링 저장
        LocalDateTime scheduleTime = LocalDateTime.now().plusMinutes(1);
        ScheduledTask task = ScheduledTask.builder()
                .auctionId(auction.getAuctionId())
                .userId(auction.getUser().getUserId())
                .scheduledTime(scheduleTime)
                .build();
        scheduledTaskRepository.save(task);

        JobDetail jobDetail = JobBuilder.newJob(TicketCreationJob.class)
                .withIdentity(auction.getAuctionUUID() + "-" + task.getScheduledTaskId(), JOB_GROUP_NAME)
                .usingJobData("auctionId", auction.getAuctionId())
                .usingJobData("userId", findUser.getUserId())
                .usingJobData("scheduledTaskId", task.getScheduledTaskId())
                .build();

        Instant instant = scheduleTime.atZone(ZoneId.systemDefault()).toInstant();
        String TRIGGER_GROUP_NAME = "ticket-creation-triggers";
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobDetail.getKey().getName(), TRIGGER_GROUP_NAME)
                .startAt(Date.from(instant))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("스케쥴링 추가");
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULING_SERVER_ERROR);
        }
    }

    @Transactional
    public void cancel(String auctionUUID, String userUUID) {
        // uuid에 해당하는 경매가 존재하는지 확인
        Auction findAuction = auctionRepository.findByAuctionUUID(auctionUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        // 찾은 경매가 로그인한 유저가 생성한 게 맞는지 확인
        if (!findAuction.getUser().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.AUCTION_ACCESS_DENIED);
        }

        // 경매 타입이 CANCEL인지 확인
        if (findAuction.getAuctionType().equals(AuctionType.CANCEL)) {
            throw new CustomException(ErrorCode.AUCTION_CONFLICT);
        }

        // 경매 타입을 CANCEL로 변경
        findAuction.changeAuctionType(AuctionType.CANCEL);
        log.info("경매가 취소되었습니다. auctionUUID : {}", findAuction.getAuctionUUID());

        // 입찰에 참여한 유저가 있을 때, 현재 최고 입찰자에게 현재 최고 입찰 금액 반환
        if (findAuction.getCurrentHighestBidderId() != null) {
            User findCurrentHighestBidder = userRepository.findByUserUUID(findAuction.getCurrentHighestBidderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            long beforePoint = findCurrentHighestBidder.getPoint();
            log.info("포인트가 추가될 예정입니다. userId : {}, 추가 전 포인트 : {}", findCurrentHighestBidder.getUserId(), beforePoint);

            findCurrentHighestBidder.increasePoint(findAuction.getCurrentHighestBidAmount());

            long afterPoint = findCurrentHighestBidder.getPoint();
            log.info("포인트가 추가되었습니다. userId : {}, 추가 후 포인트 : {}", findCurrentHighestBidder.getUserId(), afterPoint);
        }

        // 사용자 포인트에 += 최소입찰금액
        long beforePoint = findAuction.getUser().getPoint();
        log.info("포인트가 추가될 예정입니다. userId : {}, 추가 전 포인트 : {}", findAuction.getUser().getUserId(), beforePoint);

        findAuction.getUser().increasePoint(findAuction.getMinBidAmount());

        long afterPoint = findAuction.getUser().getPoint();
        log.info("포인트가 추가되었습니다. userId : {}, 추가 후 포인트 : {}", findAuction.getUser().getUserId(), afterPoint);


        ScheduledTask task = scheduledTaskRepository.findByAuctionId(findAuction.getAuctionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        // 예정된 스케줄링 작업 삭제
        JobKey jobKey = new JobKey(findAuction.getAuctionUUID() + "-" + task.getScheduledTaskId(), JOB_GROUP_NAME);

        try {
            boolean isDeleted = scheduler.deleteJob(jobKey);
            if (!isDeleted) {
                throw new CustomException(ErrorCode.FAIL_TASK_DELETE);
            }
        } catch (SchedulerException e) {
            log.info("스케줄러 취소 에러", e);
            throw new CustomException(ErrorCode.SCHEDULING_SERVER_ERROR);
        }

        // 스케줄링 타입 CANCEL로 변경
        task.changeTaskType(TaskType.CANCEL);
    }
}
