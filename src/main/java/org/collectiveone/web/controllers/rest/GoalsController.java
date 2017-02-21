package org.collectiveone.web.controllers.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.collectiveone.model.GoalState;
import org.collectiveone.model.User;
import org.collectiveone.services.GoalServiceImp;
import org.collectiveone.services.UserServiceImp;
import org.collectiveone.web.dto.Filters;
import org.collectiveone.web.dto.GoalDetachDto;
import org.collectiveone.web.dto.GoalDto;
import org.collectiveone.web.dto.GoalDtoListRes;
import org.collectiveone.web.dto.GoalGetDto;
import org.collectiveone.web.dto.GoalParentDto;
import org.collectiveone.web.dto.GoalTouchDto;
import org.collectiveone.web.dto.GoalWeightsDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rest/goals")
public class GoalsController {
	
	@Autowired
	GoalServiceImp goalService;
	
	@Autowired
	UserServiceImp userService;
	
	@RequestMapping(value="/get", method = RequestMethod.POST)
	public @ResponseBody GoalDto get(@RequestBody GoalGetDto goalGetDto) {
		return goalService.getDto(goalGetDto.getGoalTag(),goalGetDto.getProjectName());
	}
	
	@RequestMapping(value="/exist", method = RequestMethod.POST)
	public @ResponseBody boolean exist(@RequestBody GoalGetDto goalGetDto) {
		return goalService.exist(goalGetDto.getGoalTag(),goalGetDto.getProjectName(), GoalState.ACCEPTED);
	}
	
	@RequestMapping("/new")
	public @ResponseBody boolean newGoal(@Valid @RequestBody GoalDto goalDto, BindingResult result) throws IOException {
		if(result.hasErrors()) {
			return false;
		} else {
			/* check goal-tag is new in that project */
			if(!goalService.exist(goalDto.getGoalTag(),goalDto.getProjectName())) {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				User logged = userService.get(auth.getName());
				goalDto.setCreatorUsername(logged.getUsername());
				goalService.create(goalDto);
				return true;
			} else {
				return false;
			}
		}
	}
	
	@RequestMapping(value="/getSuggestions", method = RequestMethod.GET)
	public Map<String,List<String>> getList(@RequestParam("projectName") String projectName, @RequestParam("query") String query) {
		
		List<String> projectNames = new ArrayList<String>();
		if(projectName != null) {
			if(!projectName.equals("")) {
				projectNames.add(projectName);
			}
		}
		Map<String,List<String>> map = new HashMap<>();
		map.put("suggestions", goalService.getSuggestions(query, projectNames));
		return map;
	}
	
	@RequestMapping(value="/getOfProject/{projectName}", method = RequestMethod.POST)
	public @ResponseBody List<GoalDto> get(@PathVariable("projectName") String projectName) {
		Filters filters = new Filters();
		
		List<String> projectNames = new ArrayList<String>();
		projectNames.add(projectName);
		filters.setProjectNames(projectNames);
		filters.setOnlyParents(true);
		
		List<String> states = new ArrayList<String>();
		states.add(GoalState.PROPOSED.toString());
		states.add(GoalState.ACCEPTED.toString());
		filters.setStateNames(states);
		
		GoalDtoListRes goalDtos = goalService.getFilteredDto(filters);
		return goalDtos.getGoalDtos();
	}
	
	@RequestMapping(value="/getWeightData/{projectName}/{goalTag}", method = RequestMethod.POST)
	public @ResponseBody GoalWeightsDataDto getWeightData(	@PathVariable("goalTag") String goalTag,
															@PathVariable("projectName") String projectName ) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return goalService.getWeightsData(projectName, goalTag, auth.getName());
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/touch", method = RequestMethod.POST)
	public boolean touch(@RequestBody GoalTouchDto touchDto) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		touchDto.setUsername(auth.getName());
		goalService.touch(touchDto);
		return true;
	}
	
	@RequestMapping(value="/getList", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getList(@RequestBody Filters filters) {
		if(filters.getPage() == 0) filters.setPage(1);
		if(filters.getNperpage() == 0) filters.setNperpage(15);
		
		GoalDtoListRes goalsDtosRes = goalService.getFilteredDto(filters);
		
		List<GoalDto> goalDtos = goalsDtosRes.getGoalDtos();
		int[] resSet = goalsDtosRes.getResSet();
		
		Map<String,Object> map = new HashMap<>();
		
		map.put("goalDtos", goalDtos);
		map.put("resSet", resSet);
		
		return map;
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/proposeParent", method = RequestMethod.POST)
	public @ResponseBody boolean proposeParent(@RequestBody GoalParentDto goalParentDto) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(auth.getName());
		if(logged != null) {
			goalService.proposeParent(goalParentDto.getGoalId(), goalParentDto.getParentTag());
		}
		return true;
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/proposeDetach", method = RequestMethod.POST)
	public @ResponseBody boolean proposeDetach(@RequestBody GoalDetachDto goaldetachDto) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(auth.getName());
		if(logged != null) {
			goalService.detach(goaldetachDto.getGoalId(), goaldetachDto.getIncreaseBudget());
		}
		return true;
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/proposeReattach", method = RequestMethod.POST)
	public @ResponseBody boolean proposeReattach(@RequestBody GoalDetachDto goaldetachDto) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(auth.getName());
		if(logged != null) {
			goalService.reattach(goaldetachDto.getGoalId());
		}
		return true;
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/proposeIncreaseBudget", method = RequestMethod.POST)
	public @ResponseBody boolean proposeIncreaseBudget(@RequestBody GoalDetachDto goaldetachDto) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(auth.getName());
		if(logged != null) {
			goalService.increaseBudget(goaldetachDto.getGoalId(), goaldetachDto.getIncreaseBudget());
		}
		return true;
	}
}
