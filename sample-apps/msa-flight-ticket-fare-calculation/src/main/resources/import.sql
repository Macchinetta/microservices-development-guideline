INSERT INTO public.discount(discount_id, description, name) VALUES ('oneWayDiscount','一般席を利用する航空チケット予約システム利用者の通常運賃。','片道運賃');
INSERT INTO public.discount(discount_id, description, name) VALUES ('roundTripDiscount','同一路線を往復する場合の運賃。(一般席、特別席の混合不可)','往復運賃');
INSERT INTO public.discount(discount_id, description, name) VALUES ('reserve1Discount','一般席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。','予約割１');
INSERT INTO public.discount(discount_id, description, name) VALUES ('reserve7Discount','一般席を利用する航空チケット予約システム利用者が、搭乗日の 7 日前までに予約する場合に利用できる運賃。','予約割７');
INSERT INTO public.discount(discount_id, description, name) VALUES ('reserve30Discount','一般席を利用する航空チケット予約システム利用者が、搭乗日の 30 日前までに予約する場合に利用できる運賃。','早期割');
INSERT INTO public.discount(discount_id, description, name) VALUES ('ladiesDiscount','一般席を利用する女性の航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。','レディース割');
INSERT INTO public.discount(discount_id, description, name) VALUES ('groupDiscount','一般席を利用する航空チケット予約システム利用者が、3 名以上で搭乗日の前日までに予約する場合に利用できる運賃。','グループ割');
INSERT INTO public.discount(discount_id, description, name) VALUES ('specialOneWayDiscount','特別席を利用する航空チケット予約システム利用者の通常運賃。','特別片道運賃');
INSERT INTO public.discount(discount_id, description, name) VALUES ('specialRoundTripDiscount','同一路線を往復する場合の運賃。(一般席、特別席の混合不可)','特別往復運賃');
INSERT INTO public.discount(discount_id, description, name) VALUES ('specialReserve1Discount','特別席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。','特別予約割');

INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-01-01','1901-01-05','140');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-03-20','1901-03-31','140');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-08-08','1901-08-18','140');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-12-26','1901-12-31','140');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-07-18','1901-08-07','125');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-08-19','1901-08-31','125');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-12-19','1901-12-25','125');
INSERT INTO public.peak_ratio(from_date, to_date, ratio) VALUES ('1901-03-13','1901-03-19','125');

INSERT INTO public.seat_class_charge(seat_class, charge) VALUES (0, 0);
INSERT INTO public.seat_class_charge(seat_class, charge) VALUES (1, 5000);

