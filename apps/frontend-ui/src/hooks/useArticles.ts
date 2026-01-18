import { useInfiniteQuery } from "@tanstack/react-query";
import { ApiArticleRepository } from "../api/articles";

const articleRepo = new ApiArticleRepository();

export const useArticles = () => {
  return useInfiniteQuery({
    queryKey: ["articles"],
    queryFn: ({ pageParam = 0 }) => articleRepo.getAll(pageParam, 6),
    getNextPageParam: (lastPage) => {
      if (lastPage.last) return undefined;
      return lastPage.number + 1;
    },
    initialPageParam: 0,
  });
};
