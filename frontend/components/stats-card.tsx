import { Card, CardContent } from "@/components/ui/card";
import { ReactNode } from "react";

interface StatsCardProps {
  label: string;
  value: number | string;
  icon?: ReactNode;
}

export function StatsCard({ label, value, icon }: StatsCardProps) {
  return (
    <Card>
      <CardContent className="flex items-center gap-4 p-6">
        {icon && (
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted text-2xl">
            {icon}
          </div>
        )}
        <div>
          <p className="text-3xl font-bold">{value}</p>
          <p className="text-sm text-muted-foreground">{label}</p>
        </div>
      </CardContent>
    </Card>
  );
}
