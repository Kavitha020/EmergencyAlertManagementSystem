package com.eams.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eams.model.Alert;
import com.eams.model.User;
import com.eams.repository.AlertRepository;
import com.eams.repository.UserRepository;

@Service
public class AlertService {
	
	@Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    private static final double EARTH_RADIUS = 6371; // km

    public Alert createAlert(Alert alert, User user) {
        alert.setStatus("PENDING");
        if (user != null) {
            alert.setUser(user);
        } else {
            alert.setAnonymousToken(UUID.randomUUID().toString());
        }
        alert = alertRepository.save(alert);
        assignNearestResponder(alert);
        return alert;
    }

    private void assignNearestResponder(Alert alert) {
        if (alert.getAlertType() == null) return; // Avoid NPE if alertType is null
        List<User> responders = userRepository.findAvailableRespondersByAlertType(alert.getAlertType().getId());
        User nearestResponder = null;
        double minDistance = Double.MAX_VALUE;

        for (User responder : responders) {
            double distance = calculateDistance(
                alert.getLatitude(), alert.getLongitude(),
                responder.getLatitude(), responder.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearestResponder = responder;
            }
        }

        if (nearestResponder != null) {
            alert.setResponder(nearestResponder);
            alert.setStatus("ASSIGNED");
            nearestResponder.setAvailable(false);
            userRepository.save(nearestResponder);
            alertRepository.save(alert);
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public void acceptAlert(Long alertId, User responder) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        if (alert.getResponder() != null && alert.getResponder().getId().equals(responder.getId())) {
            alert.setStatus("ACCEPTED");
            alertRepository.save(alert);
        }
    }

    public void rejectAlert(Long alertId, User responder) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        if (alert.getResponder() != null && alert.getResponder().getId().equals(responder.getId())) {
            alert.setStatus("REJECTED");
            alert.getResponder().setAvailable(true);
            userRepository.save(alert.getResponder());
            alert.setResponder(null);
            alertRepository.save(alert);
            assignNearestResponder(alert);
        }
    }

    public void resolveAlert(Long alertId, User responder) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        if (alert.getResponder() != null && alert.getResponder().getId().equals(responder.getId())) {
            alert.setStatus("RESOLVED");
            alert.getResponder().setAvailable(true);
            userRepository.save(alert.getResponder());
            alertRepository.save(alert);
        }
    }

    public void adminAssignAlert(Long alertId, Long responderId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        User responder = userRepository.findById(responderId)
            .orElseThrow(() -> new IllegalArgumentException("Responder not found"));
        if ("RESPONDER".equals(responder.getRole()) && responder.isAvailable()) {
            if (alert.getResponder() != null) {
                alert.getResponder().setAvailable(true);
                userRepository.save(alert.getResponder());
            }
            alert.setResponder(responder);
            alert.setStatus("ASSIGNED");
            responder.setAvailable(false);
            userRepository.save(responder);
            alertRepository.save(alert);
        }
    }

    public List<Alert> getUserAlerts(User user) {
        return alertRepository.findByUser(user);
    }

    public List<Alert> getResponderAlerts(User responder) {
        return alertRepository.findByResponder(responder);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Alert getAlertByToken(String token) {
        return alertRepository.findByAnonymousToken(token);
    }

}
