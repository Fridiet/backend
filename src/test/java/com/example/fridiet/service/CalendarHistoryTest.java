package com.example.fridiet.service;

import com.example.fridiet.entity.Calendar;
import com.example.fridiet.entity.Fridge;
import com.example.fridiet.entity.User;
import com.example.fridiet.repository.*;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarHistoryTest {
    private final CalendarRepository calendars = mock(CalendarRepository.class);
    private final MealConsumptionRepository consumptions = mock(MealConsumptionRepository.class);
    private final CalendarService service = new CalendarService(calendars, consumptions,
            mock(InventoryBatchRepository.class), mock(FridgeRepository.class), mock(UserRepository.class),
            mock(InventoryActivityService.class), mock(RecipeService.class));

    private Calendar meal(long id) {
        Calendar meal = mock(Calendar.class);
        Fridge fridge = mock(Fridge.class);
        User user = mock(User.class);
        when(meal.getId()).thenReturn(id);
        when(meal.getFridge()).thenReturn(fridge);
        when(meal.getOwner()).thenReturn(user);
        when(fridge.getId()).thenReturn(7L);
        when(user.getId()).thenReturn(10L);
        return meal;
    }

    @Test void monthUsesHalfOpenRangeAndOneConsumptionQuery() {
        var month = YearMonth.of(2026, 12);
        var start = month.atDay(1).atStartOfDay();
        var end = month.plusMonths(1).atDay(1).atStartOfDay();
        var meals = List.of(meal(2), meal(1));
        when(calendars.findMonthHistory(10L, start, end)).thenReturn(meals);
        when(consumptions.findAllByCalendarIdInOrderByIdAsc(List.of(2L, 1L))).thenReturn(List.of());
        var result = service.getMealHistory(10L, month);
        assertEquals(List.of(2L, 1L), result.stream().map(x -> x.getMealEventId()).toList());
        verify(calendars).findMonthHistory(10L, start, end);
        verify(consumptions).findAllByCalendarIdInOrderByIdAsc(List.of(2L, 1L));
        verify(consumptions, never()).findAllByCalendarId(anyLong());
    }

    @Test void emptyMonthDoesNotQueryConsumptions() {
        when(calendars.findMonthHistory(anyLong(), any(), any())).thenReturn(List.of());
        assertTrue(service.getMealHistory(10L, YearMonth.of(2026, 2)).isEmpty());
        verifyNoInteractions(consumptions);
    }

    @Test void NoMonthPreservesExistingFullHistoryCall() {
        var meals = List.of(meal(1));
        when(calendars.findAllByOwnerIdAndDeletedAtIsNullOrderByEatenAtDesc(10L))
                .thenReturn(meals);
        when(consumptions.findAllByCalendarIdInOrderByIdAsc(List.of(1L))).thenReturn(List.of());
        assertEquals(1, service.getMealHistory(10L).size());
        verify(calendars).findAllByOwnerIdAndDeletedAtIsNullOrderByEatenAtDesc(10L);
        verify(consumptions).findAllByCalendarIdInOrderByIdAsc(List.of(1L));
    }
}
