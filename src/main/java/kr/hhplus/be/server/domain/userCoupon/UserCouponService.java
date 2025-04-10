package kr.hhplus.be.server.domain.userCoupon;

import kr.hhplus.be.server.domain.common.PageResult;
import kr.hhplus.be.server.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserService userService;
    private final UserCouponRepository userCouponRepository;

    public PageResult<UserCoupon> findAllByUserId(UserCouponCommand.FindAll command) {

        userService.findByUserId(command.userId());

        long totalCount = userCouponRepository.findCountByUserId(command.userId());

        List<UserCoupon> userCoupons = userCouponRepository.findAllByUserId(command);

        if (null == userCoupons) {
            userCoupons = new ArrayList<>();
        }

        return PageResult.create(userCoupons, command.pageNo(), command.pageSize(), totalCount);
    }

}
