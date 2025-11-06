package com.smarthome.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret:smartHomeSecretKeyForJWT2023}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;
    
    /**
     * 从JWT令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从JWT令牌中获取过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从JWT令牌中获取指定声明
     * 
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T> 声明类型
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从JWT令牌中获取所有声明
     * 
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查JWT令牌是否过期
     * 
     * @param token JWT令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 为用户生成JWT令牌
     * 
     * @param authentication 认证信息
     * @return JWT令牌
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userPrincipal.getUsername());
    }
    
    /**
     * 为用户名生成JWT令牌
     * 
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    /**
     * 为用户名生成带声明的JWT令牌
     * 
     * @param claims 声明
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return createToken(claims, username);
    }
    
    /**
     * 创建JWT令牌
     * 
     * @param claims 声明
     * @param subject 主题（用户名）
     * @return JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 生成刷新令牌
     * 
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 验证JWT令牌
     * 
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证JWT令牌格式
     * 
     * @param token JWT令牌
     * @return 是否有效格式
     */
    public Boolean validateTokenFormat(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌格式验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查令牌是否为刷新令牌
     * 
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("检查刷新令牌失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 刷新JWT令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的JWT令牌
     */
    public String refreshToken(String refreshToken) {
        try {
            Claims claims = getAllClaimsFromToken(refreshToken);
            String username = claims.getSubject();
            
            if (isTokenExpired(refreshToken)) {
                throw new JwtException("刷新令牌已过期");
            }
            
            if (!"refresh".equals(claims.get("type"))) {
                throw new JwtException("无效的刷新令牌");
            }
            
            return generateToken(username);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new JwtException("刷新令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度足够，使用Keys.secretKeyFor生成安全的密钥
        // 如果配置的密钥长度不足，则使用生成的安全密钥
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 如果配置的密钥长度不足32字节，则使用生成的安全密钥
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 从令牌中获取令牌ID
     * 
     * @param token JWT令牌
     * @return 令牌ID
     */
    public String getTokenId(String token) {
        return getClaimFromToken(token, claims -> claims.getId());
    }
    
    /**
     * 从令牌中获取签发时间
     * 
     * @param token JWT令牌
     * @return 签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }
    
    /**
     * 生成密码重置令牌
     * 
     * @param username 用户名
     * @return 密码重置令牌
     */
    public String generatePasswordResetToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "password_reset");
        
        // 密码重置令牌有效期较短，设置为1小时
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600 * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 检查令牌是否为密码重置令牌
     * 
     * @param token JWT令牌
     * @return 是否为密码重置令牌
     */
    public Boolean isPasswordResetToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "password_reset".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("检查密码重置令牌失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取令牌过期时间（秒）
     * 
     * @return 过期时间（秒）
     */
    public Long getExpirationInSeconds() {
        return jwtExpiration;
    }
}