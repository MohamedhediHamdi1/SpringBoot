package Cryptoo.com.example.Cryptoo.security;

import Cryptoo.com.example.Cryptoo.SpringApplicationContext;
import Cryptoo.com.example.Cryptoo.requests.UserLoginRequest;
import Cryptoo.com.example.Cryptoo.services.UserService;
import Cryptoo.com.example.Cryptoo.shared.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {



	private final AuthenticationManager authenticationManager;

	public AuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {
			UserLoginRequest creds = new ObjectMapper().readValue(req.getInputStream(), UserLoginRequest.class);

			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	  @Override
	    protected void successfulAuthentication(HttpServletRequest req,
	                                            HttpServletResponse res,
	                                            FilterChain chain,
	                                            Authentication auth) throws IOException, ServletException {
	        
	        String userName = ((User) auth.getPrincipal()).getUsername(); 
	        
	        String token = Jwts.builder()
	                .setSubject(userName)
	                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
	                .signWith(SignatureAlgorithm.HS512, SecurityConstants.TOKEN_SECRET )
	                .compact();
	        
	        UserService userService = (UserService)SpringApplicationContext.getBean("userServiceimpl");
	        
	        UserDto userDto = userService.getUser(userName);


	        res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);
	        res.addHeader("userid", userDto.getUserId());
	        res.addHeader("useridd", String.valueOf(userDto.getId()));



	       

	    }  
}

