import { useCallback, useEffect, useState } from 'react';
import { verifyPageConditions } from '@/api/conditions';
import { getSurveyPage } from '@/api/surveyPages';
import type { Page, SurveyPagePublic, SurveyPageShort } from '@/shared/types/Survey.type';

type EnsureResponse = () => Promise<string>;
type FlowPage = Page | SurveyPagePublic;

export function useSurveyPageFlow(
    surveyId: string,
    mode: 'preview' | 'respond',
    orderedPages: Array<Page | SurveyPageShort>,
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

    const selectPage = useCallback((pageId: string) => {
        setCurrentPageId(pageId);
        setVisitedPageIds((pageIds) => (pageIds[pageIds.length - 1] === pageId ? pageIds : [...pageIds, pageId]));
    }, []);

    const openPageById = useCallback(
        async (pageId: string): Promise<FlowPage> => {
            if (mode === 'preview') {
                const page = orderedPages.find((item): item is Page => item.id === pageId && 'questions' in item);
                if (!page) throw new Error('Страница предпросмотра не найдена');
                selectPage(page.id);
                return page;
            }

            const responseId = await ensureResponse();
            const page = await getSurveyPage(pageId, responseId);
            setLoadedPages((pages) => ({ ...pages, [page.id]: page }));
            selectPage(page.id);
            return page;
        },
        [ensureResponse, mode, orderedPages, selectPage],
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

    const openNext = useCallback(
        async (previewNextPageId?: string | null) => {
            setIsPageLoading(true);
            try {
                if (mode === 'preview') {
                    if (!previewNextPageId) return false;
                    await openPageById(previewNextPageId);
                    return true;
                }

                if (!currentPageId) return false;
                const responseId = await ensureResponse();
                const { nextPageId } = await verifyPageConditions(currentPageId, responseId);
                if (!nextPageId) return false;

                await openPageById(nextPageId);
                return true;
            } finally {
                setIsPageLoading(false);
            }
        },
        [currentPageId, ensureResponse, mode, openPageById],
    );

    const openPrevious = useCallback(() => {
        const previousPageIds = visitedPageIds.slice(0, -1);
        const previousPageId = previousPageIds[previousPageIds.length - 1];
        if (!previousPageId) return;
        setVisitedPageIds(previousPageIds);
        setCurrentPageId(previousPageId);
    }, [visitedPageIds]);

    const previewPage =
        mode === 'preview' && currentPageId
            ? orderedPages.find((item): item is Page => item.id === currentPageId && 'questions' in item)
            : undefined;
    const currentPage = mode === 'preview' ? previewPage : currentPageId ? loadedPages[currentPageId] : undefined;
    const currentPageIndex = currentPageId ? orderedPages.findIndex(({ id }) => id === currentPageId) : 0;

    return {
        currentPage,
        currentPageIndex: currentPageIndex >= 0 ? currentPageIndex : 0,
        isFirstPage: visitedPageIds.length <= 1,
        isPageLoading,
        openNext,
        openPrevious,
        start,
    };
}
