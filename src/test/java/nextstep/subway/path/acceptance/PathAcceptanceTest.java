package nextstep.subway.path.acceptance;

import static nextstep.subway.line.acceptance.testfactory.LineAcceptanceTestFactory.*;
import static nextstep.subway.line.acceptance.testfactory.LineSectionTestFactory.*;
import static nextstep.subway.station.testfactory.StationAcceptanceTestFactory.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.dto.StationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

	private static final String BASE_PATH="/paths";

	private LineResponse 신분당선;
	private LineResponse 이호선;
	private LineResponse 삼호선;
	private StationResponse 강남역;
	private StationResponse 양재역;
	private StationResponse 교대역;
	private StationResponse 남부터미널역;

	/**
	 * 교대역    --- *2호선*(10) ------   강남역
	 * |                           |
	 * *3호선*(3)                *신분당선*(10)
	 * |                          |
	 * 남부터미널역  --- *3호선*(2) ---   양재
	 */

	@BeforeEach
	public void setUp() {
		super.setUp();

		// given
		강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
		양재역 = 지하철역_등록되어_있음("양재역").as(StationResponse.class);
		교대역 = 지하철역_등록되어_있음("교대역").as(StationResponse.class);
		남부터미널역 = 지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

		신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10).as(LineResponse.class);
		이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10).as(LineResponse.class);
		삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 5).as(LineResponse.class);

		지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
	}

	@DisplayName("두 지하철 역 사이의 최단거리 경로 조회")
	@Test
	void getShortestPath() {

		// when
		PathResponse response = 최단거리_경로_요청(교대역,양재역).as(PathResponse.class);

		// then
		assertThat(response.getDistance()).isEqualTo(5);
		assertThat(response.getStations()).containsExactlyElementsOf(Arrays.asList(교대역,남부터미널역,양재역));
	}

	@DisplayName("출발역과 도착역이 같은 경우")
	@Test
	void getShortestPath2() {

		// when
		PathResponse response = 최단거리_경로_요청(교대역,교대역).as(PathResponse.class);

		// then
		assertThat(response.getDistance()).isEqualTo(0);
		assertThat(response.getStations()).containsExactlyElementsOf(Arrays.asList(교대역));
	}

	@DisplayName("존재하지 않는 출발역")
	@Test
	void getShortestPath3() {

		// given

		StationResponse 광교역 = new StationResponse(9999999L, "광교역",LocalDateTime.now(),LocalDateTime.now());

		// StationResponse 을지로역 = 지하철역_등록되어_있음("을지로역").as(StationResponse.class);
		// StationResponse 광화문역 = 지하철역_등록되어_있음("광화문역").as(StationResponse.class);
		// LineResponse 일호선 = 지하철_노선_등록되어_있음("일호선", "bg-red-600", 을지로역, 광화문역, 10).as(LineResponse.class);

		// when
		ExtractableResponse<Response>  response = 최단거리_경로_요청(광교역,교대역);

		// then
		최단거리_경로_조회_실패(response);
	}

	@DisplayName("경로가 존재하지 않을 경우")
	@Test
	void getShortestPath4() {

		// given
		StationResponse 을지로역 = 지하철역_등록되어_있음("을지로역").as(StationResponse.class);
		StationResponse 광화문역 = 지하철역_등록되어_있음("광화문역").as(StationResponse.class);
		LineResponse 일호선 = 지하철_노선_등록되어_있음("일호선", "bg-red-600", 을지로역, 광화문역, 10).as(LineResponse.class);

		// when
		ExtractableResponse<Response>  response = 최단거리_경로_요청(을지로역,교대역);

		// then
		최단거리_경로_조회_실패(response);
	}

	public ExtractableResponse<Response> 최단거리_경로_요청(StationResponse departStation, StationResponse arriveStation) {
		return RestAssured
			.given().log().all()
			.accept(MediaType.APPLICATION_JSON_VALUE)
			.when().get(BASE_PATH+"?source={departStationId}&target={arriveStationId}",
				departStation.getId(),arriveStation.getId())
			.then().log().all()
			.extract();
	}

	private void 최단거리_경로_조회_실패(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}
}
