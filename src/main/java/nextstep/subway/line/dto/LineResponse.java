package nextstep.subway.line.dto;

import nextstep.subway.line.domain.Line;
import nextstep.subway.station.dto.StationResponse;

import java.time.LocalDateTime;
import java.util.List;

public class LineResponse {
	private Long id;
	private String name;
	private String color;
	private List<StationResponse> stations;
	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;
	private int extraPrice;

	public LineResponse() {
	}

	public LineResponse(Long id, String name, String color,
		List<StationResponse> stations, LocalDateTime createdDate, LocalDateTime modifiedDate, int extraPrice) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.stations = stations;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.extraPrice = extraPrice;
	}

	public static LineResponse of(Line line, List<StationResponse> stations) {
		return new LineResponse(line.getId(), line.getName(), line.getColor(), stations, line.getCreatedDate(),
			line.getModifiedDate(),
			line.getExtraPrice());
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public List<StationResponse> getStations() {
		return stations;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public int getExtraPrice() {
		return extraPrice;
	}
}