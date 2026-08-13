import { useCallback, useEffect, useState } from 'react';
import { verifyPageConditions } from '@/api/conditions';
import { getSurveyPage } from '@/api/surveyPages';
import type { SurveyPagePublic, SurveyPageShort } from '@/shared/types/Survey.type';

type EnsureResponse = () => Promise<string>;

export function useSurveyPageFlow(
    surveyId: string,
    mode: 'preview' | 'respond',
    orderedPages: SurveyPageShort[],
    ensureResponse: EnsureResponse,
) {
    const [currentPageId, setCurrentPageId] = useState<string | null>(null);
    const [visitedPageIds, setVisitedPageIds] = useState<string[]>([]);
    const [loadedPages, setLoadedPages] = useState<Record<string, SurveyPagePublic>>({});
    const [isPageLoading, setIsPageLoading] = useState(false);

    useEffect(() => {
        setCurrentPageId(null);
        setVisitedPageIds([]);
        setLoadedPages({});
        setIsPageLoading(false);
    }, [mode, surveyId]);

    const openPageById = useCallback(
        async (pageId: string) => {
            const responseId = await ensureResponse();
            const page = await getSurveyPage(pageId, responseId);
            setLoadedPages((pages) => ({ ...pages, [page.id]: page }));
            setCurrentPageId(page.id);
            setVisitedPageIds((pageIds) => (pageIds[pageIds.length - 1] === page.id ? pageIds : [...pageIds, page.id]));
            return page;
        },
        [ensureResponse],
    );

    const start = useCallback(async () => {
        const firstPage = orderedPages[0];
        if (!firstPage) return null;

        setIsPageLoading(true);
        try {
            return await openPageById(firstPage.id);
        } finally {
            setIsPageLoading(false);
        }
    }, [openPageById, orderedPages]);

    const verifyAndOpenNext = useCallback(
        async (pageId: string) => {
            setIsPageLoading(true);
            try {
                const responseId = await ensureResponse();
                const { nextPageId } = await verifyPageConditions(pageId, responseId);
                if (!nextPageId) return false;

                await openPageById(nextPageId);
                return true;
            } finally {
                setIsPageLoading(false);
            }
        },
        [ensureResponse, openPageById],
    );

    const openPrevious = useCallback(() => {
        const previousPageId = visitedPageIds[visitedPageIds.length - 2];
        if (!previousPageId) return;

        setVisitedPageIds((pageIds) => pageIds.slice(0, -1));
        setCurrentPageId(previousPageId);
    }, [visitedPageIds]);

    const currentPage = currentPageId ? loadedPages[currentPageId] : undefined;
    const currentPageIndex = currentPageId ? orderedPages.findIndex(({ id }) => id === currentPageId) : 0;

    return {
        currentPage,
        currentPageIndex: currentPageIndex >= 0 ? currentPageIndex : 0,
        isFirstPage: visitedPageIds.length <= 1,
        isPageLoading,
        openPrevious,
        start,
        verifyAndOpenNext,
    };
}
