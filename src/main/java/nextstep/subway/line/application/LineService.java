package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Sections;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.line.exception.NotExistLineException;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
	private LineRepository lineRepository;
	private StationService stationService;

	public LineService(LineRepository lineRepository, StationService stationService) {
		this.lineRepository = lineRepository;
		this.stationService = stationService;
	}

	public LineResponse saveLine(LineRequest request) {
		Station upStation = stationService.findById(request.getUpStationId());
		Station downStation = stationService.findById(request.getDownStationId());
		Line persistLine = lineRepository.save(
			request.toLine(upStation, downStation));
		Sections sections = persistLine.getSections();
		List<StationResponse> stations = StationService.converToStationResponses(sections.getOrderedStations());
		return LineResponse.of(persistLine, stations);
	}

	public List<LineResponse> findLines() {
		List<Line> persistLines = lineRepository.findAll();

		return persistLines.stream()
			.map(line -> {
				Sections sections = line.getSections();
				List<StationResponse> stations = StationService.converToStationResponses(sections.getOrderedStations());
				return LineResponse.of(line, stations);
			})
			.collect(Collectors.toList());
	}

	public Line findLineById(Long id) {
		return lineRepository.findById(id).orElseThrow(NotExistLineException::new);
	}

	public LineResponse findLineResponseById(Long id) {
		Line persistLine = findLineById(id);
		Sections sections = persistLine.getSections();
		List<StationResponse> stations = StationService.converToStationResponses(sections.getOrderedStations());
		return LineResponse.of(persistLine, stations);
	}

	public void updateLine(Long id, LineRequest lineUpdateRequest) {
		Line persistLine = lineRepository.findById(id)
			.orElseThrow(RuntimeException::new);
		persistLine.update(new Line(lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
	}

	public void deleteLineById(Long id) {
		lineRepository.deleteById(id);
	}

	public void addLineStation(Long lineId, SectionRequest request) {
		Line line = findLineById(lineId);
		Sections sections = line.getSections();
		Station upStation = stationService.findStationById(request.getUpStationId());
		Station downStation = stationService.findStationById(request.getDownStationId());
		sections.addSection(line, upStation, downStation, request.getDistance());
	}

	public void removeLineStation(Long lineId, Long stationId) {
		Line line = findLineById(lineId);
		Station station = stationService.findStationById(stationId);
		Sections sections = line.getSections();
		sections.removeStation(line, station);
	}

	public List<Line> findAllExistStations(List<Station> stations) {
		return lineRepository.findAllExistStations(stations);
	}
}