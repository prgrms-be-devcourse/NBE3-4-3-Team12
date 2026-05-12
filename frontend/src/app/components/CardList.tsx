import Card from "./Card";

type CardListProps = {
  groups: {
    id: string;
    title: string;
    category: { id: number; type: string; name: string }[];
    status: string;
  }[];
};

const CardList = ({ groups }: CardListProps) => {
  if (!groups || groups.length === 0) {
    return <p className="text-center text-[var(--text-soft)]">등록된 그룹이 없습니다.</p>;
  }

  return (
    <div className="flex justify-center">
      <div className="grid w-full max-w-6xl grid-cols-1 gap-6 p-2 sm:grid-cols-2 lg:grid-cols-3">
        {groups.map((group) => (
          <Card key={group.id} {...group} />
        ))}
      </div>
    </div>
  );
};

export default CardList;
