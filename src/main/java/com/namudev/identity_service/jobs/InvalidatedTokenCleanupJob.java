package com.namudev.identity_service.jobs;

import com.namudev.identity_service.service.InvalidatedTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvalidatedTokenCleanupJob {
    InvalidatedTokenService invalidatedTokenService;

//    @Scheduled(
//            cron = "...",          // Chạy theo biểu thức thời gian cực linh hoạt.
//            fixedRate = ...,       // Chạy mỗi N mili-giây, tính từ lúc job bắt đầu
//            fixedDelay = ...,      // Chạy lại sau N mili-giây kể từ khi job trước KẾT THÚC.
//            initialDelay = ...,    // Trì hoãn lần chạy đầu tiên
//            zone = "Asia/Ho_Chi_Minh" // múi giờ cho cron, để cố định múi giờ.
//    )
//      ==> Chỉ cần một trong các nhóm: cron, hoặc fixedRate, hoặc fixedDelay.

//Cron có 6 phần:
//┌───────────── second (0–59)
//│ ┌─────────── minute (0–59)
//│ │ ┌───────── hour (0–23)
//│ │ │ ┌─────── day of month (1–31)
//│ │ │ │ ┌───── month (1–12)
//│ │ │ │ │ ┌─── day of week (0–7, Sunday = 0/7)
//│ │ │ │ │ │
//        * * * * * *
//    ➡ Ví dụ hay dùng:
//      0 * * * * *             Mỗi phút (khi giây = 0)
//      0 0 * * * *             Mỗi giờ
//      0 0 2 * * *             2:00 sáng hàng ngày
//      0 0 0 * * MON           0h sáng thứ Hai hàng tuần
//      0 0/15 * * * *          Mỗi 15 phút
//      0 30 9-17 * * MON-FRI   9h30–17h30, thứ Hai–thứ Sáu
//      0 0 0 1 * *             0h ngày đầu tiên mỗi tháng

//    @Scheduled(cron = "* * 0 * * ?")
    public void run() {
        try {
            int deleted = invalidatedTokenService.purgeExpiredToken();
            log.info("[Cleanup] Purged {} expired invalidated tokens", deleted);
        } catch (Exception e) {
            log.error("[Cleanup] Failed to purge expired tokens", e);
        }
    }
}
